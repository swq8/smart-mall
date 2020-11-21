package alex.controller;

import alex.authentication.UserToken;
import alex.cache.GoodsCache;
import alex.cache.GoodsSpecCache;
import alex.entity.GoodsEntity;
import alex.lib.Helper;
import alex.lib.HelperUtils;
import alex.repository.UserAddressRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(path = "cart")
public class Cart {

    @Resource
    UserAddressRepository userAddressRepository;

    @GetMapping(path = "")
    public ModelAndView getIndex(HttpServletRequest request) {
        var cart = new alex.lib.Cart(request);
        ModelAndView modelAndView = Helper.newModelAndView("cart/index", request);
        modelAndView.addObject("cartItems", cart.getItems());
        return modelAndView;
    }

    @GetMapping(path = "add")
    public ModelAndView getAdd(HttpServletRequest request) {
        long goodsId = Helper.longValue(request.getParameter("gid"));
        long specId = Helper.longValue(request.getParameter("specId"));
        long num = Helper.longValue(request.getParameter("num"));
        GoodsEntity goodsEntity = GoodsCache.getGoodsEntity(goodsId);
        var goodsSpecEntities = GoodsSpecCache.getGoodsSpecEntities(goodsId);
        var cart = new alex.lib.Cart(request);
        cart.add(goodsId, specId, num);
        ModelAndView modelAndView = Helper.newModelAndView("cart/add", request);
        modelAndView.addObject("goodsEntity", goodsEntity);
        return modelAndView;
    }

    @GetMapping(path = "buy")
    public ModelAndView getBuy(HttpServletRequest request) {
        UserToken userToken = (UserToken) request.getAttribute(UserToken.KEY);
        var addresses = userAddressRepository.findAllByUserId(userToken.getId());
        ModelAndView modelAndView = Helper.newModelAndView("cart/buy", request);
        modelAndView.addObject("addresses", addresses);
        var cart = new alex.lib.Cart(request);
        for (var item : cart.getItems()) {
            item.setGoodsImg(HelperUtils.imgZoom(item.getGoodsImg(), 60, 60));
        }
        modelAndView.addObject("cart", cart);
        modelAndView.addObject("title", "结算");
        return modelAndView;
    }


    @GetMapping(path = "json", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getJson(HttpServletRequest request) {
        long goodsId = Helper.longValue(request.getParameter("goodsId"));
        long specId = Helper.longValue(request.getParameter("specId"));
        boolean selected = Helper.longValue(request.getParameter("selected")) > 0;
        long num = Helper.longValue(request.getParameter("num"));
        long imgWidth = Helper.longValue(request.getParameter("w"));
        if (imgWidth <= 0) {
            imgWidth = 60;
        }
        String method = request.getParameter("m");
        if (method == null) {
            method = "";
        }

        String json = null;
        var cart = new alex.lib.Cart(request);
        switch (method) {
            case "add":
                cart.add(goodsId, specId, num);
                break;
            case "clear":
                cart.clear();
                break;
            case "del":
                cart.del(goodsId, specId);
                break;
            case "selected":
                cart.setSelected(goodsId, specId, selected);
                break;
            case "selectedAll":
                cart.setSelectedAll(selected);
                break;
            case "sub":
                cart.sub(goodsId, specId, num);
                break;
        }

        for (var item : cart.getItems()) {
            item.setGoodsImg(HelperUtils.imgZoom(item.getGoodsImg(), imgWidth, imgWidth));
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            json = objectMapper.writeValueAsString(cart.getItems());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }
}
