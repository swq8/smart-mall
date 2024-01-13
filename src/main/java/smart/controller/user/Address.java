package smart.controller.user;

import jakarta.annotation.Resource;
import smart.auth.UserToken;
import smart.cache.RegionCache;
import smart.cache.SystemCache;
import smart.entity.UserAddressEntity;
import smart.util.Helper;
import smart.util.Json;
import smart.lib.JsonResult;
import smart.util.ValidatorUtils;
import smart.repository.UserAddressRepository;
import smart.service.UserAddressService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

import java.util.Objects;

@Controller
@RequestMapping(path = "user/address")
@Transactional
public class Address {

    @Resource
    UserAddressRepository userAddressRepository;

    @Resource
    UserAddressService userAddressService;


    @GetMapping(path = "")
    public ModelAndView getIndex(HttpServletRequest request, UserToken userToken) {
        ModelAndView modelAndView = Helper.newModelAndView("user/address/index", request);
        modelAndView.addObject("addresses", userAddressRepository.findAllByUserIdOrderByDftDesc(userToken.getId()));
        modelAndView.addObject("title", SystemCache.getSiteName() + " - 收货地址");
        return modelAndView;
    }

    @PostMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postIndex(HttpServletRequest request, UserToken userToken) {
        String back = request.getParameter("back");
        if (back == null) {
            back = "/user/address";
        }
        JsonResult jsonResult = new JsonResult();
        long id = Helper.parseNumber(request.getParameter("id"), Long.class);
        String consignee = request.getParameter("consignee");
        if (consignee == null || consignee.trim().isEmpty()) {
            jsonResult.setMsg("收货人姓名不得为空");
            return jsonResult.toString();
        }
        consignee = consignee.trim();
        String phone = request.getParameter("phone");
        String msg = ValidatorUtils.notEmpty(phone, "联系电话");
        if (msg != null) {
            jsonResult.setMsg(msg);
            return jsonResult.toString();
        }
        msg = ValidatorUtils.validateMobile(phone, "联系电话");
        if (msg != null) {
            jsonResult.setMsg(msg);
            return jsonResult.toString();
        }
        String address = request.getParameter("address");
        msg = ValidatorUtils.notEmpty(address, "详细地址");
        if (msg != null) {
            jsonResult.setMsg(msg);
            return jsonResult.toString();
        }
        address = address.trim();
        msg = ValidatorUtils.validateAddress(address, "详细地址");
        if (msg != null) {
            jsonResult.setMsg(msg);
            return jsonResult.toString();
        }
        long code = Helper.parseNumber(request.getParameter("code"), Long.class);
        if (RegionCache.getRegion(code) == null) {
            jsonResult.setMsg("请选择 县/区");
            return jsonResult.toString();
        }
        UserAddressEntity userAddressEntity;
        if (id > 0) {
            userAddressEntity = userAddressRepository.findByIdAndUserId(id, userToken.getId());
            if (userAddressEntity == null) {
                jsonResult.setMsg("要修改的地址不存在");
                return jsonResult.toString();
            }
        } else {
            userAddressEntity = new UserAddressEntity();
            userAddressEntity.setUserId(userToken.getId());
        }
        userAddressEntity.setAddress(address);
        userAddressEntity.setConsignee(consignee);
        userAddressEntity.setPhone(phone);
        userAddressEntity.setRegion(code);
        if (Helper.parseNumber(request.getParameter("dft"), Long.class) > 0) {
            userAddressEntity.setDft(1L);
        }
        if (id > 0) {
            userAddressService.updateAddress(userAddressEntity);
        } else {
            msg = userAddressService.addAddress(userAddressEntity);
            if (msg != null) {
                jsonResult.setMsg(msg);
                return jsonResult.toString();
            }
        }
        // 对有addrId需求的请求回调时带上addrId
        if (request.getParameter("addrId") != null) {
            back += "?addrId=" + userAddressEntity.getId();
        }
        jsonResult.setUrl(back);
        return jsonResult.toString();
    }

    @GetMapping(path = "delete", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getDelete(HttpServletRequest request, UserToken userToken) {
        long id = Helper.parseNumber(request.getParameter("id"), Long.class);
        UserAddressEntity addressEntity = userAddressRepository.findByIdAndUserId(id, userToken.getId());
        if (addressEntity == null || !Objects.equals(addressEntity.getUserId(), userToken.getId())) {
            return "{\"err\":\"地址不存在\"}";
        }
        userAddressRepository.delete(addressEntity);
        return null;
    }

    @GetMapping(path = "json", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getJson(HttpServletRequest request, UserToken userToken) {
        long id = Helper.parseNumber(request.getParameter("id"), Long.class);
        UserAddressEntity addressEntity = userAddressRepository.findByIdAndUserId(id, userToken.getId());
        if (addressEntity == null || !Objects.equals(addressEntity.getUserId(), userToken.getId())) {
            return "{\"err\":\"地址不存在\"}";
        }
        String json = Json.stringify(addressEntity);
        if (json == null) {
            return "{\"err\":\"系统错误\"}";
        }
        return json;
    }

    @GetMapping(path = "setDft", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getSetDft(HttpServletRequest request, UserToken userToken) {

        long id = Helper.parseNumber(request.getParameter("id"), Long.class);
        UserAddressEntity addressEntity = userAddressRepository.findByIdAndUserId(id, userToken.getId());
        if (addressEntity == null || !Objects.equals(addressEntity.getUserId(), userToken.getId())) {
            return "{\"err\":\"地址不存在\"}";
        }
        userAddressService.setDefault(addressEntity);
        return null;
    }
}
