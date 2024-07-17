package smart.controller.user;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import smart.auth.UserToken;
import smart.cache.PaymentCache;
import smart.cache.SystemCache;
import smart.dto.OrderQueryDto;
import smart.entity.OrderEntity;
import smart.entity.OrderGoodsEntity;
import smart.entity.UserEntity;
import smart.lib.JsonResult;
import smart.lib.payment.Payment;
import smart.repository.OrderRepository;
import smart.service.OrderGoodsService;
import smart.service.OrderService;
import smart.util.DbUtils;
import smart.util.Helper;
import smart.util.LogUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping(path = "user/order")
@Transactional
public class Order {

    @Resource
    OrderRepository orderRepository;

    @Resource
    OrderService orderService;

    @Resource
    OrderGoodsService orderGoodsService;

    /**
     * 取消订单
     *
     * @param request   request
     * @param userToken user token
     * @return view
     */
    @GetMapping(path = "cancel")
    public ModelAndView getCancel(HttpServletRequest request, UserToken userToken) {
        // 订单号
        long no = Helper.parseNumber(request.getParameter("no"), Long.class);
        var userEntity = DbUtils.findByIdForWrite(userToken.getId(), UserEntity.class);
        var orderEntity = orderRepository.findByNoAndUserIdForWrite(no, userEntity.getId());
        if (orderEntity == null) {
            return Helper.msgPage(String.format("订单 %s 不存在", no), "/user/order", request);
        }
        String err = orderService.cancelOrder(userEntity, orderEntity);
        if (err == null) {
            return Helper.msgPage(String.format("订单 %s 已取消", no), "/user/order", request);
        }
        return Helper.msgPage(String.format("订单 %s 取消失败", no), "/user/order", request);
    }

    /**
     * 确认收货
     *
     * @param request   request
     * @param userToken user token
     * @return view
     */
    @GetMapping(path = "confirm")
    public ModelAndView getConfirm(HttpServletRequest request, UserToken userToken) {
        // 订单号
        long no = Helper.parseNumber(request.getParameter("no"), Long.class);
        String err = orderService.confirmOrder(userToken.getId(), no);
        if (err == null) {
            return Helper.msgPage(String.format("订单 %s 已确认收货", no), "/user/order", request);
        }
        return Helper.msgPage(String.format("订单 %s 确认收货失败", no), "/user/order", request);
    }

    /**
     * 移入回收站/删除订单
     *
     * @param request   request
     * @param userToken user token
     * @return view
     */
    @GetMapping(path = "delete")
    public ModelAndView getDelete(HttpServletRequest request, UserToken userToken) {
        // 订单号
        long no = Helper.parseNumber(request.getParameter("no"), Long.class);
        int deleted = Helper.parseNumber(request.getParameter("deleted"), Integer.class);
        String err = orderService.deleteOrder(userToken.getId(), no, deleted);

        String msg = switch (deleted) {
            case 0 -> "订单恢复";
            case 1 -> "订单移入回收站";
            case 2 -> "订单删除";
            default -> "";
        };
        if (err == null) {
            return Helper.msgPage(msg + "已完成 " + no, "/user/order", request);

        }
        return Helper.msgPage(msg + "失败 " + no, "/user/order", request);
    }

    /**
     * 订单详情
     *
     * @param request   request
     * @param userToken user token
     * @return view
     */
    @GetMapping(path = "detail")
    public ModelAndView getDetail(HttpServletRequest request, UserToken userToken) {
        // 订单号
        long no = Helper.parseNumber(request.getParameter("no"), Long.class);
        OrderEntity orderEntity = orderRepository.findByNo(no);
        if (orderEntity == null || !Objects.equals(orderEntity.getUserId(), userToken.getId())) {
            return Helper.msgPage("订单不存在", null, request);
        }
        List<OrderGoodsEntity> goodsList = orderGoodsService.findByOrderNo(orderEntity.getNo());

        ModelAndView modelAndView = Helper.newModelAndView("user/order/detail", request);
        modelAndView.addObject("goodsList", goodsList);
        modelAndView.addObject("order", orderEntity);
        modelAndView.addObject("title", "订单详情");

        return modelAndView;
    }

    @GetMapping(path = "")
    public ModelAndView getIndex(
            HttpServletRequest request,
            UserToken userToken,
            @RequestParam(name = "deleted", defaultValue = "0") String deletedStr,
            @RequestParam(name = "status", defaultValue = "") String statusStr) {
        Long deleted = Helper.parseNumber(deletedStr, Long.class);
        if (deleted < 0 || deleted > 1) {
            deleted = 0L;
        }
        var query = new OrderQueryDto();
        query.setDeleted(deleted);
        query.setUid(userToken.getId());
        if (StringUtils.hasText(statusStr)) {
            query.setStatus(Helper.parseNumber(statusStr, Long.class));
        }
        query.setSort("id,desc");

        ModelAndView view = Helper.newModelAndView("user/order/index", request);
        var pagination = orderService.query(query);
        view.addObject("pagination", pagination);
        view.addObject("title", SystemCache.getSiteName() + " - 我的订单");
        return view;
    }

    /**
     * 支付订单
     *
     * @param request   request
     * @param userToken user token
     * @return view
     */
    @GetMapping(path = "pay")
    public ModelAndView getPay(HttpServletRequest request, UserToken userToken) {
        var orderNo = Helper.parseNumber(request.getParameter("orderNo"), Long.class);
        OrderEntity orderEntity = orderRepository.findByNo(orderNo);
        if (orderEntity == null || !Objects.equals(orderEntity.getUserId(), userToken.getId())) {
            return Helper.msgPage("订单不存在 " + orderNo, request);
        }
        if (orderEntity.getStatus() > 0 || orderEntity.getPayTime() != null) {
            return Helper.msgPage("订单状态错误" + orderNo, request);
        }
        Payment payment = null;
        for (var item : PaymentCache.getPayments()) {
            if (item.getName().equals(orderEntity.getPayName())) {
                payment = item;
            }
        }
        if (payment == null) {
            return Helper.msgPage("该订单的支付方式已不被支持，请从新下单。 " + orderNo, request);
        }

        ModelAndView modelAndView = Helper.newModelAndView("user/order/pay", request);
        modelAndView.addObject("order", orderEntity);
        modelAndView.addObject("payment", payment);
        modelAndView.addObject("title", SystemCache.getSiteName() + " - 支付订单");
        return modelAndView;
    }

    /**
     * 查询订单支付状态, 用于客户端轮询.
     *
     * @param request http request
     * @return json
     */
    @GetMapping(path = "payCheck", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getPayCheck(HttpServletRequest request, UserToken userToken) {
        JsonResult jsonResult = new JsonResult();
        var orderNo = Helper.parseNumber(request.getParameter("orderNo"), Long.class);

        OrderEntity orderEntity = orderRepository.findByNo(orderNo);
        if (orderEntity == null || !Objects.equals(orderEntity.getUserId(), userToken.getId())) {
            jsonResult.setMsg("订单不存在").setUrl("/user/order");
            return Helper.msgPage(jsonResult, request);
        }

        if (orderEntity.getStatus() > 0 && orderEntity.getPayTime() != null) {
            jsonResult.setMsg("订单支付成功").setUrl("/user/order");
            return Helper.msgPage(jsonResult, request);
        }
        return jsonResult.toString();
    }

    /**
     * 获取支付二维码
     *
     * @param request   http request
     * @param response  http response
     * @param userToken user token
     */
    @GetMapping("payQr")
    @ResponseBody
    public String getPayQR(HttpServletRequest request, HttpServletResponse response, UserToken userToken) {
        var orderNo = Helper.parseNumber(request.getParameter("orderNo"), Long.class);
        OrderEntity orderEntity = orderRepository.findByNo(orderNo);
        if (orderEntity == null || !Objects.equals(orderEntity.getUserId(), userToken.getId())) {
            return "订单不存在 " + orderNo;
        }
        if (orderEntity.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return "订单金额错误 " + orderNo;
        }
        Payment payment = PaymentCache.getPaymentByName(orderEntity.getPayName());
        if (payment == null) {
            return "该订单的支付方式已不被支持，请从新下单";
        }
        // 图片宽度
        int width = Helper.parseNumber(request.getParameter("w"), Integer.class);


        if (width < 300) {
            width = 300;
        }
        String qrStr;
        try {
            qrStr = payment.getQrCode("订单:" + orderNo, Long.toString(orderNo), orderEntity.getAmount());
        } catch (Exception e) {
            LogUtils.error(getClass(), "%s orderNo:%s %s ".formatted(request.getRequestURI(), orderNo, e.getMessage()));
            return "出错了，请联系管理员";
        }
        response.setHeader("Cache-Control", "no-store");
        response.setContentType("image/png");
        byte[] buffer = Helper.getQRCodePng(qrStr, width);
        assert buffer != null;
        try {
            response.getOutputStream().write(buffer);
        } catch (IOException e) {
            LogUtils.error(getClass(), e);
        }
        return null;
    }
}
