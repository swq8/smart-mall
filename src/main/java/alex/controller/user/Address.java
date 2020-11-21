package alex.controller.user;

import alex.authentication.UserToken;
import alex.cache.RegionCache;
import alex.entity.UserAddressEntity;
import alex.lib.Helper;
import alex.lib.JsonResult;
import alex.lib.Validate;
import alex.repository.UserAddressRepository;
import alex.service.UserAddressService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(path = "user/address")
public class Address {

    @Resource
    UserAddressRepository userAddressRepository;

    @Resource
    UserAddressService userAddressService;


    @GetMapping(path = "")
    public ModelAndView getIndex(HttpServletRequest request) {
        UserToken userToken = (UserToken) request.getAttribute(UserToken.KEY);
        ModelAndView modelAndView = Helper.newModelAndView("user/address/index", request);
        modelAndView.addObject("addresses", userAddressRepository.findAllByUserId(userToken.getId()));
        return modelAndView;
    }

    @PostMapping(path = "", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String postIndex(HttpServletRequest request) {
        UserToken userToken = (UserToken) request.getAttribute(UserToken.KEY);
        String back = request.getParameter("back");
        if (back == null) {
            back = "/user/address";
        }
        JsonResult jsonResult = new JsonResult();
        long id = Helper.longValue(request.getParameter("id"));
        String name = request.getParameter("name");
        if (name == null || name.trim().length() == 0) {
            jsonResult.setMsg("收货人姓名不得为空");
            return jsonResult.toString();
        }
        name = name.trim();
        String phone = request.getParameter("phone");
        String msg = Validate.notEmpty(phone, "联系电话");
        if (msg != null) {
            jsonResult.setMsg(msg);
            return jsonResult.toString();
        }
        msg = Validate.mobile(phone, "联系电话");
        if (msg != null) {
            jsonResult.setMsg(msg);
            return jsonResult.toString();
        }
        String address = request.getParameter("address");
        msg = Validate.notEmpty(address, "详细地址");
        if (msg != null) {
            jsonResult.setMsg(msg);
            return jsonResult.toString();
        }
        address = address.trim();
        msg = Validate.address(address, "详细地址");
        if (msg != null) {
            jsonResult.setMsg(msg);
            return jsonResult.toString();
        }
        long code = Helper.longValue(request.getParameter("code"));
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
        userAddressEntity.setName(name);
        userAddressEntity.setPhone(phone);
        userAddressEntity.setRegion(code);
        if (Helper.longValue(request.getParameter("dft")) > 0) {
            userAddressEntity.setDft(1);
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
        jsonResult.setUrl(back);
        return jsonResult.toString();
    }

    @GetMapping(path = "delete", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getDelete(HttpServletRequest request) {
        UserToken userToken = (UserToken) request.getAttribute(UserToken.KEY);
        long id = Helper.longValue(request.getParameter("id"));
        UserAddressEntity addressEntity = userAddressRepository.findByIdAndUserId(id, userToken.getId());
        if (addressEntity == null || addressEntity.getUserId() != userToken.getId()) {
            return "{\"err\":\"地址不存在\"}";
        }
        userAddressRepository.delete(addressEntity);
        return null;
    }

    @GetMapping(path = "json", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getJson(HttpServletRequest request) {
        UserToken userToken = (UserToken) request.getAttribute(UserToken.KEY);
        long id = Helper.longValue(request.getParameter("id"));
        UserAddressEntity addressEntity = userAddressRepository.findByIdAndUserId(id, userToken.getId());
        if (addressEntity == null || addressEntity.getUserId() != userToken.getId()) {
            return "{\"err\":\"地址不存在\"}";
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(addressEntity);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "{\"err\":\"系统错误\"}";
        }
    }

    @GetMapping(path = "setDft", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getSetDft(HttpServletRequest request) {
        UserToken userToken = (UserToken) request.getAttribute(UserToken.KEY);
        long id = Helper.longValue(request.getParameter("id"));
        UserAddressEntity addressEntity = userAddressRepository.findByIdAndUserId(id, userToken.getId());
        if (addressEntity == null || addressEntity.getUserId() != userToken.getId()) {
            return "{\"err\":\"地址不存在\"}";
        }
        userAddressService.setDefault(addressEntity);
        return null;
    }
}
