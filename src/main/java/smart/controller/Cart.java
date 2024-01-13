package smart.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import smart.auth.UserToken;
import smart.cache.PaymentCache;
import smart.cache.SystemCache;
import smart.entity.UserAddressEntity;
import smart.entity.UserEntity;
import smart.lib.JsonResult;
import smart.lib.status.OrderStatus;
import smart.lib.thymeleaf.HelperUtils;
import smart.repository.UserAddressRepository;
import smart.service.OrderService;
import smart.util.DbUtils;
import smart.util.Helper;
import smart.util.Json;

import java.math.BigDecimal;

@Controller
@RequestMapping(path = "cart")
@Transactional
public class Cart {

    @Resource
    OrderService orderService;

    @Resource
    UserAddressRepository userAddressRepository;

    @GetMapping(path = "")
    public ModelAndView getIndex(HttpServletRequest request) {
        var cart = new smart.lib.Cart(request);
        ModelAndView modelAndView = Helper.newModelAndView("cart/index", request);
        modelAndView.addObject("cartItems", cart.getItems());
        modelAndView.addObject("title", SystemCache.getSiteName() + " - 购物车");
        return modelAndView;
    }

    /**
     * 商品加入购物车
     *
     * @param request req
     * @return 视图
     */
    @GetMapping(path = "add")
    public ModelAndView getAdd(HttpServletRequest request) {
        long goodsId = Helper.parseNumber(request.getParameter("gid"), Long.class);
        long specId = Helper.parseNumber(request.getParameter("specId"), Long.class);
        long num = Helper.parseNumber(request.getParameter("num"), Long.class);
        var cart = new smart.lib.Cart(request);
        cart.add(goodsId, specId, num);
        return Helper.msgPage("已加入购物车", "/cart", request);
    }

    /**
     * 购买页面
     *
     * @param request request
     * @return view
     */
    @GetMapping(path = "buy")
    public ModelAndView getBuy(HttpServletRequest request,
                               UserToken userToken) {
        var cart = new smart.lib.Cart(request);
        if (cart.sumNum() == 0) {
            return Helper.msgPage("购物车中没有选中的商品", "/cart", request);
        }
        var userEntity = DbUtils.findById(userToken.getId(), UserEntity.class);
        var addresses = userAddressRepository.findAllByUserIdOrderByDftDesc(userToken.getId());
        long addrId = Helper.parseNumber(request.getParameter("addrId"), Long.class);
        UserAddressEntity address = null;
        for (var addr : addresses) {
            if (addr.getId() == addrId) {
                address = addr;
                break;
            }
        }
        if (address == null && !addresses.isEmpty()) {
            address = addresses.getFirst();
            addrId = address.getId();
        }
        ModelAndView view = Helper.newModelAndView("cart/buy", request);
        view.addObject("balance", userEntity.getBalance());
        view.addObject("addresses", addresses);
        view.addObject("address", address);

        for (var item : cart.getItems()) {
            item.setGoodsImg(HelperUtils.imgZoom(item.getGoodsImg(), 60, 60));
        }
        view.addObject("addrId", addrId);
        view.addObject("cart", cart);
        //运费
        long shippingFee = 0;
        if (address != null) {
            shippingFee = cart.getShippingFee(address.getRegion());
        }
        view.addObject("payments", PaymentCache.getAvailablePayments());
        view.addObject("shippingFee", shippingFee);
        view.addObject("title", SystemCache.getSiteName() + " - 结算");
        return view;
    }


    /**
     * 提交订单
     *
     * @param request req
     * @return re
     */
    @PostMapping(path = "buy", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postBuy(HttpServletRequest request,
                          @RequestParam(defaultValue = "", name = "addrId") String addrIdStr,
                          @RequestParam(defaultValue = "", name = "payBalance") String payBalanceStr,
                          @RequestParam(defaultValue = "", name = "shippingFee") String shippingFeeStr,
                          @RequestParam(defaultValue = "", name = "sumPrice") String sumPriceStr) {
        JsonResult jsonResult = new JsonResult();
        long addrId = Helper.parseNumber(addrIdStr, Long.class);
        if (addrId < 1) return jsonResult.setMsg("请选择收货地址").toString();
        String payName = request.getParameter("payName");
        // 验证提交时的运费和商品价格
        long payBalance = Helper.parseNumber(payBalanceStr, BigDecimal.class).multiply(new BigDecimal(100)).longValue();
        long shippingFee = Helper.parseNumber(shippingFeeStr, BigDecimal.class).multiply(new BigDecimal(100)).longValue();
        long sumPrice = Helper.parseNumber(sumPriceStr, BigDecimal.class).multiply(new BigDecimal(100)).longValue();
        smart.lib.Cart cart = new smart.lib.Cart(request);
        var orderInfo = orderService.addOrder(
                addrId, payBalance, payName, cart, sumPrice, shippingFee, Helper.isMobileRequest(request) ? 2 : 1
        );
        if (orderInfo.getErr() != null) {
            return jsonResult.setMsg(orderInfo.getErr()).toString();
        }
        if (orderInfo.getOrderStatus() == OrderStatus.WAIT_FOR_SHIPPING.getCode())
            return jsonResult.setMsg("已完成下单").setUrl("/user/order").toString();
        return jsonResult.setUrl("/user/order/pay?orderNo=" + orderInfo.getOrderNo()).toString();
    }

    @GetMapping(path = "json", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getJson(HttpServletRequest request) {
        long goodsId = Helper.parseNumber(request.getParameter("goodsId"), Long.class);
        long specId = Helper.parseNumber(request.getParameter("specId"), Long.class);
        boolean selected = Helper.parseNumber(request.getParameter("selected"), Long.class) > 0;
        long num = Helper.parseNumber(request.getParameter("num"), Long.class);
        long imgWidth = Helper.parseNumber(request.getParameter("w"), Long.class);
        if (imgWidth <= 0) {
            imgWidth = 60;
        }
        String method = request.getParameter("m");
        if (method == null) {
            method = "";
        }

        var cart = new smart.lib.Cart(request);
        switch (method) {
            case "add" -> cart.add(goodsId, specId, num);
            case "clear" -> cart.clear();
            case "del" -> cart.del(goodsId, specId);
            case "selected" -> cart.setSelected(goodsId, specId, selected);
            case "selectedAll" -> cart.setSelectedAll(selected);
            case "sub" -> cart.sub(goodsId, specId, num);
        }

        for (var item : cart.getItems()) {
            item.setGoodsImg(HelperUtils.imgZoom(item.getGoodsImg(), imgWidth));
        }
        return Json.stringify(cart.getItems());
    }
}
