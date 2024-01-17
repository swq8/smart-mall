package smart.service;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.StringUtils;
import smart.cache.ExpressCache;
import smart.cache.PaymentCache;
import smart.cache.RegionCache;
import smart.config.AppConfig;
import smart.entity.*;
import smart.lib.Cart;
import smart.lib.Pagination;
import smart.lib.payment.Payment;
import smart.lib.status.OrderGoodsStatus;
import smart.lib.status.OrderStatus;
import smart.repository.GoodsSpecRepository;
import smart.repository.OrderRepository;
import smart.repository.UserAddressRepository;
import smart.repository.UserRepository;
import smart.util.DbUtils;
import smart.util.Helper;
import smart.util.SqlBuilder;
import smart.dto.OrderQueryDto;

import java.sql.Timestamp;
import java.util.*;

@Service
@Transactional
public class OrderService {
    static String[] SORTABLE_COLUMNS = new String[]{
            "amount", "create_time", "pay_time"
    };

    @Resource
    JdbcClient jdbcClient;

    @Resource
    GoodsSpecRepository goodsSpecRepository;

    @Resource
    OrderRepository orderRepository;

    @Resource
    OrderGoodsService orderGoodsService;

    @Resource
    UserAddressRepository userAddressRepository;

    @Resource
    UserService userService;

    @Resource
    UserRepository userRepository;

    /**
     * 根据订单ID生成订单号
     *
     * @param orderId 订单ID
     * @return 订单号
     */
    public static long generalOrderNo(long orderId) {
        String str = Helper.dateFormat(new Date(), "yyMMdd");
        String str1 = String.format("%06d", orderId * 997 % 1000000);
        return Helper.parseNumber(str + str1, Long.class);
    }

    @NotNull
    private static OrderGoodsEntity getOrderGoodsEntity(Cart.Item item, OrderEntity orderEntity) {
        OrderGoodsEntity orderGoodsEntity = new OrderGoodsEntity();
        orderGoodsEntity.setOrderNo(orderEntity.getNo());
        orderGoodsEntity.setGoodsId(item.getGoodsId());
        orderGoodsEntity.setGoodsName(item.getGoodsName());
        orderGoodsEntity.setNum(item.getNum());
        orderGoodsEntity.setSpecId(item.getSpecId());
        orderGoodsEntity.setSpecDes(item.getSpecDes());
        orderGoodsEntity.setPrice(item.getGoodsPrice());
        orderGoodsEntity.setWeight(item.getGoodsWeight());
        return orderGoodsEntity;
    }

    /**
     * 添加订单
     *
     * @param addressId   地址ID
     * @param payBalance  balance pay
     * @param cart        用户购物车
     * @param sumPrice    商品总金额,用于效验
     * @param shippingFee 物流费,用于效验
     * @param source      订单来源 1电脑网页,2移动端网页,3微信公众号,4微信小程序
     * @return 订单信息
     */
    public OrderInfo addOrder(long addressId, long payBalance, String payName, Cart cart, long sumPrice, long shippingFee, long source) {
        OrderInfo orderInfo = new OrderInfo();
        long orderAmount = sumPrice + shippingFee;
        long userId = cart.getUserToken().getId();
        UserEntity userEntity = DbUtils.findByIdForWrite(userId, UserEntity.class);
        if (userEntity == null || userEntity.getStatus() != 0) {
            return orderInfo.setErr("用户不存在或状态异常");
        }
        UserAddressEntity addressEntity = userAddressRepository.findByIdAndUserId(addressId, userId);
        if (addressEntity == null) {
            return orderInfo.setErr("收货地址不存在");
        }
        if (cart.sumPrice() != sumPrice || cart.getShippingFee(addressEntity.getRegion()) != shippingFee || cart.sumNum() == 0) {
            return orderInfo.setErr("购物车被修改，请重新提交");
        }
        if (payBalance < 0) {
            return orderInfo.setErr("余额支付数值错误");
        }
        if (orderAmount < payBalance) {
            payBalance = orderAmount;
        }
        if (payBalance > userEntity.getBalance()) {
            return orderInfo.setErr("可用余额不足");
        }
        if (payBalance < sumPrice) {
            if (payName == null) {
                return orderInfo.setErr("请选择支付方式");
            }
            if (!PaymentCache.getAvailableNames().contains(payName)) {
                orderInfo.setErr("指定的支付方式不存在:" + payName);
            }
        }


        //要购买的商品,完成后从购物车清除
        Set<Cart.Item> cartItems = new LinkedHashSet<>();
        for (var item : cart.getItems()) {
            if (!item.isSelected()) {
                continue;
            }
            cartItems.add(item);
        }
        // 优化锁行顺序, 按商品ID排序后顺序锁行
        List<Long> goodsIds = new ArrayList<>();
        for (var item : cartItems) {
            goodsIds.add(item.getGoodsId());
        }
        Collections.sort(goodsIds);
        Map<Long, GoodsEntity> goodsEntityMap = new HashMap<>();
        for (var goodsId : goodsIds) {
            try {
                goodsEntityMap.put(goodsId, DbUtils.findByIdForWrite(goodsId, GoodsEntity.class));
            } catch (CannotAcquireLockException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return orderInfo.setErr("系统繁忙");
            }
        }

        for (var item : cartItems) {
            GoodsEntity goodsEntity = goodsEntityMap.get(item.getGoodsId());
            if (goodsEntity == null || !goodsEntity.getOnSell()) {
                return orderInfo.setErr("商品已下架");
            }
            // 库存
            if (item.getSpecId() == 0) {
                if (goodsEntity.getStock() < item.getNum()) {
                    return orderInfo.setErr("库存不足:" + goodsEntity.getName());
                }
            } else {
                GoodsSpecEntity goodsSpecEntity;
                try {
                    goodsSpecEntity = goodsSpecRepository.findByIdForWrite(item.getSpecId());
                } catch (CannotAcquireLockException e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return orderInfo.setErr("系统繁忙");
                }
                if (goodsSpecEntity == null || goodsSpecEntity.getGoodsId() != item.getGoodsId()) {
                    return orderInfo.setErr("规格数据错误:" + goodsEntity.getName());
                }
                if (goodsSpecEntity.getStock() < item.getNum()) {
                    return orderInfo.setErr("库存不足:" + goodsEntity.getName());
                }
            }
        }
        OrderEntity orderEntity = new OrderEntity();
        // 订单ID
        orderEntity.setId(AppConfig.getOrderId().incrementAndGet());
        // 订单号
        orderEntity.setNo(generalOrderNo(orderEntity.getId()));
        // 订单金额
        orderEntity.setAmount(orderAmount);
        orderEntity.setAddress(addressEntity.getAddress());
        orderEntity.setPayBalance(payBalance);
        orderEntity.setConsignee(addressEntity.getConsignee());
        orderEntity.setCreateTime(new Timestamp(System.currentTimeMillis()));
        orderEntity.setPayName(payName);
        orderEntity.setPhone(addressEntity.getPhone());
        orderEntity.setRegion(addressEntity.getRegion());
        orderEntity.setShippingFee(shippingFee);
        orderEntity.setStatus(OrderStatus.WAIT_FOR_PAY.getCode());
        orderEntity.setSource(source);
        orderEntity.setUserId(userId);

        // 0额订单或余额支付订单无需在线支付
        if (payBalance == orderAmount) {
            orderEntity.setStatus(OrderStatus.WAIT_FOR_SHIPPING.getCode());
            orderEntity.setPayTime(orderEntity.getCreateTime());
        }


        // order goods entity to insert
        List<OrderGoodsEntity> orderGoodsEntities = new LinkedList<>();
        // 扣库存，创建订单商品
        cartItems.forEach(item -> {
            if (item.getSpecId() == 0) {
                jdbcClient.sql("update t_goods set stock=stock - ? where id = ?")
                        .params(item.getNum(), item.getGoodsId())
                        .update();
            } else {
                jdbcClient.sql("update t_goods_spec set stock=stock - ? where id = ?")
                        .params(item.getNum(), item.getSpecId())
                        .update();
            }
            OrderGoodsEntity orderGoodsEntity = getOrderGoodsEntity(item, orderEntity);
            orderGoodsEntities.add(orderGoodsEntity);
        });
        if (payBalance > 0) {
            var msg = userService.changeBalance(userEntity, -payBalance, "支付订单:" + orderEntity.getNo());
            if (msg != null) {
                DbUtils.rollback();
                return orderInfo.setErr(msg);
            }
        }
        DbUtils.insertAll(orderGoodsEntities);
        DbUtils.insert(orderEntity);

        cart.del(cartItems);
        orderInfo.setOrderNo(orderEntity.getNo())
                .setAmount(orderAmount)
                .setOrderStatus(orderEntity.getStatus());
        return orderInfo;
    }

    /**
     * 取消订单
     *
     * @param orderNo order no
     * @return null(成功) or 错误信息
     */
    public String cancelOrder(UserEntity userEntity, OrderEntity orderEntity) {
        if (orderRepository.cancelOrder(orderEntity.getNo()) == 0) {
            return "订单状态错误,仅能取消待付款、待发货订单";
        }
        // 增加库存
        orderGoodsService.backOrderGoods(orderEntity.getNo());
        refundOrder(userEntity, orderEntity, false);
        return null;
    }


    /**
     * 确认收货,确认时验证用户是否拥有该订单
     *
     * @param userId  user id
     * @param orderNo order no
     * @return null(成功) or 错误信息
     */
    public String confirmOrder(long userId, long orderNo) {
        if (jdbcClient.sql("update t_order set status=3, confirm_time=NOW() where no = ? and user_id = ? and status = 2")
                .params(orderNo, userId).update() == 0) {
            return "订单信息错误";
        }
        jdbcClient.sql("update t_order_goods set status = ? where order_no = ?")
                .params(OrderGoodsStatus.RECEIVED.getCode(), orderNo).update();
        return null;
    }

    /**
     * 设置订单删除状态, 复原/回收站/删除
     *
     * @param userId  user id
     * @param orderNo order no
     * @param deleted deleted status
     * @return null(成功) or 错误信息
     */
    public String deleteOrder(long userId, long orderNo, int deleted) {
        if (deleted < 0 || deleted > 2) {
            return "删除参数不正确";
        }
        String sql = "update t_order set deleted=? where no = ? and user_id = ? and deleted=? and status > 2";
        int tmp = 0;
        int oldDeleted = deleted == 1 ? 0 : 1;
        if (jdbcClient.sql(sql).params(deleted, orderNo, userId, oldDeleted).update() == 0) {
            return "订单信息错误";
        }
        return null;
    }

    public OrderEntity getOrder(long orderNo) {
        var orderEntity = orderRepository.findByNo(orderNo);
        if (orderEntity == null) {
            return null;
        }
        orderEntity.setPayBalanceStr(Helper.priceFormat(orderEntity.getPayBalance()));
        orderEntity.setAmountStr(Helper.priceFormat(orderEntity.getAmount()));
        orderEntity.setExpressName(ExpressCache.getNameById(orderEntity.getExpressId()));
        var payment = PaymentCache.getPaymentByName(orderEntity.getPayName());
        orderEntity.setPayNameCn(payment == null ? orderEntity.getPayName() : payment.getNameCn());
        orderEntity.setStatusName(OrderStatus.getStatusName(orderEntity.getStatus()));
        orderEntity.setOrderGoods(orderGoodsService.getOrderGoods(orderNo));
        orderEntity.setRegionStr(Objects.requireNonNull(RegionCache.getRegion(orderEntity.getRegion())).toString());
        return orderEntity;
    }

    /**
     * 获取用户订单,用户端使用
     *
     * @param userId      user id
     * @param pageSize    page size
     * @param page        current page
     * @param keyWord     key word
     * @param isDeleted   is deleted
     * @param orderStatus order status, all status if null
     * @return user orders
     */
    public Pagination getUserOrders(long userId, long pageSize, long page, String keyWord, boolean isDeleted, OrderStatus orderStatus) {
        String sql = "select id, no, user_id as userId,region from t_order";
        return Pagination.newBuilder(null).build();
    }

    public String adminPay(Long orderNo) {
        OrderEntity orderEntity = orderRepository.findByNoForWrite(orderNo);
        if (orderEntity == null) {
            return "订单不存在," + orderNo;
        }
        if (orderRepository.adminPay(orderNo) == 0) {
            return "订单状态错误," + orderNo;
        }
        return null;
    }

    /**
     * 支付订单
     *
     * @param orderNo   订单号
     * @param payName   支付方式
     * @param payAmount 支付金额
     * @param payNo     支付流水号
     * @return null 成功，或返回错误信息
     */
    public String pay(long orderNo, String payName, long payAmount, String payNo) {
        if (payName == null) {
            return "支付方式不得为空";
        }
        OrderEntity orderEntity = orderRepository.findByNoForWrite(orderNo);
        if (orderEntity == null) {
            return "订单不存在";
        }
        if (orderEntity.getStatus() != OrderStatus.WAIT_FOR_PAY.getCode()) {
            return "该订单不是待支付订单";
        }
        jdbcClient.sql("update t_order set pay_name = ?,pay_time = ?,pay_amount = ?,pay_no =?,status = 1 where no = ?")
                .params(payName, new Timestamp(System.currentTimeMillis()), payAmount, payNo, orderNo).update();
        return null;
    }

    public Pagination query(OrderQueryDto query) {
        var sql = """
                SELECT a.consignee,
                       date_format(a.create_time, '%Y-%m-%d %T') as create_time,
                       a.id,
                       a.no,
                       date_format(a.pay_time, '%Y-%m-%d %T')    as pay_time,
                       a.pay_amount,
                       a.pay_no,
                       a.amount,
                       a.status,
                       a.user_id,
                       b.name                                    as user_name
                FROM t_order a
                         LEFT JOIN t_user b ON a.user_id = b.id
                                """;
        List<Object> sqlParams = new ArrayList<>();
        sql = new SqlBuilder(sql, sqlParams)
                .andLikeIfNotBlank("no", query.getNo())
                .andTrimEqualsIfNotBlank("b.name", query.getName())
                .andEqualsIfNotNull("a.status", query.getStatus())
                .orderBy(SORTABLE_COLUMNS, query.getSort(), "create_time,desc")
                .buildSql();

        var pagination = Pagination.newBuilder(sql, sqlParams.toArray())
                .page(query.getPage())
                .pageSize(query.getPageSize())
                .build();
        pagination.getRows().forEach(row -> {
            row.put("amountStr", Helper.priceFormat(Helper.parseNumber(row.get("amount"), Long.class)));
            row.put("statusName", OrderStatus.getStatusName((long) row.get("status")));

        });
        return pagination;
    }

    /**
     * cancel ship
     *
     * @param orderNo 订单号
     * @return null 成功，或返回错误信息
     */
    public String cancelShip(Long orderNo) {
        if (orderNo == null) {
            return "订单号不得为空";
        }
        OrderEntity orderEntity = orderRepository.findByNoForWrite(orderNo);
        if (orderEntity == null) {
            return "订单不存在, no:" + orderNo;
        }

        if (orderRepository.cancelShip(orderNo) == 0L) {
            return "订单状态错误";
        }
        DbUtils.update(OrderGoodsEntity.class,
                Map.of("orderNo", orderNo),
                Map.of("status", OrderGoodsStatus.UNSHIPPED.getCode()));
        return null;
    }

    /**
     * 订单发货
     *
     * @param orderNo 订单号
     * @return null 成功，或返回错误信息
     */
    public String ship(Long orderNo, Long expressId, String expressNo) {
        if (orderNo == null) {
            return "订单号不得为空";
        }
        if (expressId == null || ExpressCache.getNameById(expressId) == null) {
            return "快递公司id错误,express id:" + expressId;
        }
        OrderEntity orderEntity = orderRepository.findByNoForWrite(orderNo);
        if (orderEntity == null) {
            return "订单不存在, no:" + orderNo;
        }
        if (!StringUtils.hasText(expressNo)) {
            return "快递单号不得为空";
        }
        if (orderRepository.ship(orderNo, expressId, expressNo.trim()) == 0L) {
            return "订单状态错误";
        }
        DbUtils.update(OrderGoodsEntity.class,
                Map.of("orderNo", orderNo),
                Map.of("status", OrderGoodsStatus.SHIPPED.getCode()));
        return null;
    }

    public String refundOrder(Long orderNo, Boolean orderStatusOnly) {
        var orderEntity = orderRepository.findByNoForWrite(orderNo);
        if (orderEntity == null) {
            return "订单不存在, no:" + orderNo;
        }
        var userEntity = DbUtils.findByIdForWrite(orderEntity.getUserId(), UserEntity.class);
        return refundOrder(userEntity, orderEntity, orderStatusOnly);
    }

    /**
     * 订单退款
     *
     * @param orderNo order no
     * @return null(成功) or 错误信息
     */
    public String refundOrder(UserEntity userEntity, OrderEntity orderEntity, Boolean orderStatusOnly) {
        var statusList = List.of(OrderStatus.WAIT_FOR_SHIPPING.getCode(),
                OrderStatus.SHIPPED.getCode(),
                OrderStatus.COMPLETED.getCode());
        if (!statusList.contains(orderEntity.getStatus())) {
            return "订单状态错误";
        }
        if (!orderStatusOnly) {
            if (orderEntity.getPayBalance() > 0) {
                userService.changeBalance(userEntity, orderEntity.getPayBalance(),
                        "订单退款:" + orderEntity.getNo());
            }
            // 订单金额大于0的原路退回
            if (orderEntity.getPayAmount() > 0) {
                Payment payment = PaymentCache.getPaymentByName(orderEntity.getPayName());
                if (payment != null) {
                    var msg = payment.refund(orderEntity.getNo(), orderEntity.getPayAmount());
                    if (msg != null) {
                        return msg;
                    }
                }
            }
        }
        orderEntity.setPayAmount(0L);
        orderEntity.setPayBalance(0L);
        orderEntity.setStatus(OrderStatus.REFUND.getCode());
        DbUtils.update(orderEntity, "status");
        jdbcClient.sql("update t_order_goods set status = ? where order_no = ?")
                .params(OrderGoodsStatus.RETURNED.getCode(), orderEntity.getNo())
                .update();

        return null;
    }

    public static class OrderInfo {
        private String err;
        private long orderNo;
        private long orderStatus;
        // 订单金额
        private long amount;

        public String getErr() {
            return err;
        }

        public OrderInfo setErr(String err) {
            this.err = err;
            return this;
        }

        public long getOrderNo() {
            return orderNo;
        }

        public OrderInfo setOrderNo(long orderNo) {
            this.orderNo = orderNo;
            return this;
        }

        public long getOrderStatus() {
            return orderStatus;
        }

        public OrderInfo setOrderStatus(long orderStatus) {
            this.orderStatus = orderStatus;
            return this;
        }

        public long getAmount() {
            return amount;
        }

        public OrderInfo setAmount(long amount) {
            this.amount = amount;
            return this;
        }
    }
}
