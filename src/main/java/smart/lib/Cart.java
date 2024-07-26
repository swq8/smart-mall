package smart.lib;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import smart.auth.UserToken;
import smart.cache.ExpressCache;
import smart.config.AppConfig;
import smart.config.RedisConfig;
import smart.entity.GoodsSpecEntity;
import smart.lib.session.Session;
import smart.repository.GoodsRepository;
import smart.repository.GoodsSpecRepository;
import smart.util.Helper;
import smart.util.Json;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Cart {
    public static String NAME = "cart";
    private final HttpServletRequest request;
    private final UserToken userToken;
    private final List<Item> items;

    private GoodsRepository goodsRepository;
    private GoodsSpecRepository goodsSpecRepository;

    public Cart(HttpServletRequest request) {
        init();
        this.request = request;
        String json;
        userToken = (UserToken) request.getAttribute(StringUtils.uncapitalize(UserToken.class.getSimpleName()));
        if (userToken == null) {
            json = (String) Session.from(request).get(NAME);
        } else {
            json = RedisConfig.getStringRedisTemplate().opsForValue().get(NAME + ":" + userToken.getId());
        }
        if (json == null || json.length() < 10) {
            items = new LinkedList<>();
        } else {
            items = Json.parseList(json, Item.class);
        }
        if (!check()) {
            save();
        }
    }

    private void init() {
        goodsRepository = AppConfig.getContext().getBean(GoodsRepository.class);
        goodsSpecRepository = AppConfig.getContext().getBean(GoodsSpecRepository.class);
    }

    public void add(Item item) {
        add(item.goodsId, item.specId, item.num);
    }

    public void add(long goodsId, long specId, long num) {
        if (goodsId <= 0 || specId < 0 || num <= 0) {
            return;
        }
        for (Item item : items) {
            if (item.goodsId == goodsId && item.specId == specId) {
                item.num += num;
                save();
                return;
            }

        }
        var item = new Item(goodsId, specId, num);
        items.addFirst(item);
        save();
    }

    /**
     * Check goods availability
     *
     * @return all goods availability
     */
    public boolean check() {
        int size = items.size();

        items.removeIf(item -> {
            var goodsEntity = goodsRepository.findById(item.goodsId).orElse(null);
            if (goodsEntity == null
                    //已下架商品
                    || !goodsEntity.getOnSell()) {
                return true;
            }
            item.setGoodsImg(goodsEntity.getImgsObj().getFirst());
            item.setGoodsName(goodsEntity.getName());
            item.setGoodsWeight(goodsEntity.getWeight());
            item.setGoodsPrice(goodsEntity.getPrice());
            item.setShippingFee(goodsEntity.getShippingFee());
            if (item.specId == 0) {
                item.setSpecDes("");
                return false;
            }
            var specEntities = goodsSpecRepository.findByGoodsId(item.getGoodsId());
            if (specEntities == null) {
                return true;
            }
            GoodsSpecEntity specEntity = null;
            for (GoodsSpecEntity entity : specEntities) {
                if (entity.getId() == item.getSpecId()) {
                    specEntity = entity;
                    break;
                }
            }
            if (specEntity == null) {
                return true;
            } else {
                item.setGoodsPrice(specEntity.getPrice());
                item.setGoodsWeight(specEntity.getWeight());
                item.setSpecDes(specEntity.getDes());
                return false;
            }

        });
        return size == items.size();
    }

    /**
     * 清空购物车
     */
    public void clear() {
        items.clear();
        save();
    }

    /**
     * 删除商品
     *
     * @param goodsId 商品ID
     * @param specId  商品规格
     */
    public void del(long goodsId, long specId) {
        if (goodsId <= 0 || specId < 0) {
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            var item = items.get(i);
            if (item.goodsId == goodsId && item.specId == specId) {
                items.remove(i);
                break;
            }
        }
        save();
    }

    /**
     * 批量删除商品
     *
     * @param items 商品
     */
    public void del(Collection<Item> items) {
        items.forEach(this.items::remove);
        save();
    }

    public List<Item> getItems() {
        return items;
    }

    public UserToken getUserToken() {
        return userToken;
    }

    /**
     * 保存购物车数据
     */
    void save() {
        check();
        String json;
        List<Map<Object, Object>> list = new LinkedList<>();
        items.forEach(item -> {
            Map<Object, Object> map = Map.of(
                    "goodsId", item.getGoodsId(),
                    "specId", item.getSpecId(),
                    "num", item.getNum(),
                    "selected", item.isSelected()
            );
            list.add(map);
        });
        json = Json.stringify(list);
        if (userToken == null) {
            Session.from(request).set(NAME, json);
        } else {
            assert json != null;
            RedisConfig.getStringRedisTemplate().opsForValue().set(NAME + ":" + userToken.getId(), json);
        }


    }

    /**
     * 设置指定商品选中状态
     *
     * @param goodsId  商品ID
     * @param specId   商品规格
     * @param selected 选中状态
     */
    public void setSelected(long goodsId, long specId, boolean selected) {
        if (goodsId <= 0 || specId < 0) {
            return;
        }
        for (Item item : items) {
            if (item.goodsId == goodsId && item.specId == specId) {
                if (item.isSelected() != selected) {
                    item.setSelected(selected);
                    save();
                }
                return;
            }
        }
    }

    /**
     * 设置所有商品选中状态
     *
     * @param selected 选中状态
     */
    public void setSelectedAll(boolean selected) {
        items.forEach(item -> item.setSelected(selected));
        save();
    }

    /**
     * 获取物流费用
     *
     * @param code 收货地代码
     * @return 物流费用, 负数不支持该地区派送
     */
    public BigDecimal getShippingFee(long code) {
        var feeRule = ExpressCache.getFeeRule();
        // 常规运费
        BigDecimal shippingFee = feeRule.getShippingFee(code, sumWeight());
        // 不能送达返回-1
        if (shippingFee.doubleValue() < 0) {
            return BigDecimal.valueOf(-1);
        }

        // iss there a free shipping item
        if (hasFreeShippingGoods()) {
            return BigDecimal.ZERO;
        }

        // 是否符合免邮费规则
        var freeRule = ExpressCache.getFreeRule();
        if (freeRule.isEnable()
                // 地址未被包邮规则排除
                && !freeRule.getExclude().contains(code)
                // 金额达到包邮金额
                && sumPrice().compareTo(freeRule.getAmount()) >=0) {
            return BigDecimal.ZERO;
        }
        // 以上规则都不符合，返回常规运费
        return shippingFee;
    }

    /**
     * has shipping fee free goods in selected
     *
     * @return bool
     */
    public boolean hasFreeShippingGoods() {
        return items.stream().anyMatch(item -> item.selected && !item.shippingFee);
    }

    /**
     * 添加商品
     *
     * @param goodsId 商品ID
     * @param specId  商品规格
     * @param num     商品数量
     */
    public void sub(long goodsId, long specId, long num) {
        if (goodsId <= 0 || specId < 0 || num <= 0) {
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            var item = items.get(i);
            if (item.goodsId == goodsId && item.specId == specId) {
                if (item.num > num) {
                    item.num -= num;
                } else {
                    items.remove(i);
                }
                save();
                return;
            }
        }
    }

    /**
     * 选中商品数量合计
     *
     * @return 选中商品数量合计
     */
    public long sumNum() {
        long l = 0;
        for (var item : items) {
            if (item.isSelected()) {
                l += item.num;
            }
        }
        return l;
    }

    /**
     * 选中商品价格合计
     *
     * @return 选中商品价格合计
     */
    public BigDecimal sumPrice() {
        BigDecimal sum = BigDecimal.ZERO;
        for (var item : items) {
            if (item.isSelected()) {
                sum = sum.add(item.goodsPrice.multiply(BigDecimal.valueOf(item.num)));
            }
        }
        return sum;
    }

    /**
     * 选中商品重量合计
     *
     * @return 选中商品重量合计
     */
    public long sumWeight() {
        long l = 0;
        for (var item : items) {
            if (item.isSelected()) {
                l += item.goodsWeight * item.num;
            }
        }
        return l;
    }


    public static class Item {
        private long goodsId;

        private String goodsImg;

        private String goodsName;

        private BigDecimal goodsPrice;

        private long goodsWeight;

        private boolean shippingFee;
        private long specId;

        private boolean selected = true;
        private String specDes;
        private long num;

        public Item() {
        }

        public Item(long goodsId, long specId, long num) {
            this.goodsId = goodsId;
            this.specId = specId;
            this.num = num;
        }

        @Override
        public int hashCode() {
            return (int) (goodsId + specId);
        }

        @Override
        public boolean equals(Object anObject) {
            if (anObject == null) {
                return false;
            }
            return (anObject instanceof Item item && item.hashCode() == hashCode());
        }

        public long getGoodsId() {
            return goodsId;
        }

        public String getGoodsImg() {
            return goodsImg;
        }

        public void setGoodsImg(String goodsImg) {
            this.goodsImg = goodsImg;
        }

        public String getGoodsName() {
            return goodsName;
        }

        public void setGoodsName(String goodsName) {
            this.goodsName = goodsName;
        }

        public BigDecimal getGoodsPrice() {
            return goodsPrice;
        }

        public void setGoodsPrice(BigDecimal goodsPrice) {
            this.goodsPrice = goodsPrice;
        }


        public long getGoodsWeight() {
            return goodsWeight;
        }

        public void setGoodsWeight(long goodsWeight) {
            this.goodsWeight = goodsWeight;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public boolean getShippingFee() {
            return shippingFee;
        }

        public void setShippingFee(boolean shippingFee) {
            this.shippingFee = shippingFee;
        }

        public long getSpecId() {
            return specId;
        }

        public String getSpecDes() {
            return specDes;
        }

        public void setSpecDes(String specDes) {
            this.specDes = specDes;
        }

        public long getNum() {
            return num;
        }

        public void setNum(long num) {
            this.num = num;
        }
    }
}
