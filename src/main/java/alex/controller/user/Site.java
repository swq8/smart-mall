package alex.controller.user;

import alex.Application;
import alex.authentication.UserToken;
import alex.entity.UserEntity;
import alex.lib.Cart;
import alex.lib.*;
import alex.repository.UserRepository;
import alex.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller(value = "user/site")
@RequestMapping(path = "user")
public class Site {

    @Resource
    UserRepository userRepository;

    @Resource
    UserService userService;


    @GetMapping(path = "central")
    public ModelAndView getCentral(HttpServletRequest request) {
        UserToken userToken = (UserToken) request.getAttribute(UserToken.KEY);
        ModelAndView modelAndView = Helper.newModelAndView("user/central", request);
        UserEntity userEntity = userRepository.findById(userToken.getId()).orElse(null);
        modelAndView.addObject("userEntity", userEntity);
        return modelAndView;
    }

    @GetMapping(path = "info")
    public ModelAndView getInfo(HttpServletRequest request) {
        UserToken userToken = (UserToken) request.getAttribute(UserToken.KEY);
        ModelAndView modelAndView = Helper.newModelAndView("user/info", request);
        UserEntity userEntity = userRepository.findById(userToken.getId()).orElse(null);
        modelAndView.addObject("userEntity", userEntity);
        return modelAndView;
    }

    @GetMapping(path = "login")
    public ModelAndView getLogin(HttpServletRequest request) {
        if (request.getParameter("logout") != null) {
            Helper.flushSession(request.getSession(false));
            request.removeAttribute(UserToken.KEY);
        }
        ModelAndView modelAndView = Helper.newModelAndView("user/login", request);
        modelAndView.addObject("back", request.getParameter("back"));
        return modelAndView;
    }

    @PostMapping(path = "info", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String postInfo(
            HttpServletRequest request,
            @RequestParam(required = false) String phone
    ) {
        UserToken userToken = (UserToken) request.getAttribute(UserToken.KEY);
        String msg;
        JsonResult jsonResult = new JsonResult();
        msg = Validate.mobile(phone, "手机号");
        if (msg != null) {
            jsonResult.error.put("phone", msg);
        }

        if (jsonResult.error.size() == 0) {
            if (userRepository.updateInfo(userToken.getId(), phone) == 0) {
                jsonResult.setMsg("修改失败,请刷新重试");
            } else {
                jsonResult.setMsg("修改成功");
                jsonResult.setUrl("/user/central");
                userToken.setPhone(phone);
                userToken.save(request.getSession());

            }
        }
        return jsonResult.toString();
    }

    @PostMapping(path = "login", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String postLogin(
            HttpServletRequest request,
            @RequestParam String name,
            @RequestParam String password
    ) {
        HttpSession session = request.getSession();
        String back = request.getParameter("back");
        if (back == null || back.length() == 0) {
            back = "/";
        }
        Cart oldCart = null;
        if (UserToken.from(session) == null) {
            oldCart = new Cart(request);
        }
        JsonResult jsonResult = new JsonResult();
        UserService.Result result = userService.login(name, password, Helper.getClientIp(request));
        if (result.userEntity != null) {
            //login success
            UserToken userToken = new UserToken(result.userEntity);
            userToken.save(session);
            request.setAttribute(UserToken.KEY, userToken);
            Cart cart = new Cart(request);
            if (oldCart != null) {
                oldCart.getItems().forEach(item -> cart.add(item.getGoodsId(), item.getSpecId(), item.getNum()));
            }
            jsonResult.setUrl(back);

        } else {
            result.errors.forEach(jsonResult.error::put);

        }
        return jsonResult.toString();

    }

    @GetMapping(path = "password")
    public ModelAndView getPassword(HttpServletRequest request) {
        return Helper.newModelAndView("user/password", request);
    }

    @PostMapping(path = "password", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String postPassword(
            HttpServletRequest request,
            @RequestParam(required = false) String oldPassword,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String password1
    ) {
        UserToken userToken = (UserToken) request.getAttribute(UserToken.KEY);
        String msg;
        JsonResult jsonResult = new JsonResult();
        msg = Validate.password(oldPassword, "原密码");
        if (msg != null) {
            jsonResult.error.put("oldPassword", msg);
        }
        msg = Validate.password(password, "新密码");
        if (msg == null) {
            if (password.equals(oldPassword)) {
                jsonResult.error.put("password", "新旧密码相同");
            }
        } else {
            jsonResult.error.put("password", msg);
        }

        if (jsonResult.error.size() == 0 && !password.equals(password1)) {
            jsonResult.error.put("password1", "重复密码与新密码不一致");
        }
        if (jsonResult.error.size() == 0) {
            String hash = Crypto.sha3_256(oldPassword + userToken.getSalt());
            if (!userToken.getPassword().equals(hash)) {
                jsonResult.error.put("oldPassword", "原密码错误");
            }
        }
        if (jsonResult.error.size() == 0) {
            UserService userService = Application.CONTEXT.getBean(UserService.class);
            String salt = userService.editPassword(userToken.getId(), password, Helper.getClientIp(request));
            if (salt != null) {
                jsonResult.setMsg("修改成功");
                jsonResult.setUrl("/user/central");
                userToken.setPassword(Crypto.sha3_256(password + salt));
                userToken.setSalt(salt);
                userToken.save(request.getSession());
            } else {
                jsonResult.setMsg("修改失败,请刷新重试  ");
            }
        }
        return jsonResult.toString();
    }

    @GetMapping(path = "register")
    public ModelAndView getRegister(HttpServletRequest request) {
        return Helper.newModelAndView("user/register", request);
    }

    @PostMapping(path = "register", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String postRegister(
            HttpServletRequest request,
            @RequestParam(required = false) String captcha,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String password1
    ) {

        HttpSession session = request.getSession(false);
        if (name != null) {
            name = name.toLowerCase();
        }
        JsonResult result = new JsonResult();
        String msg = Validate.name(name, "用户名");
        if (msg != null) {
            result.error.put("name", msg);
        }

        msg = Validate.password(password, "密码");
        if (msg != null) {
            result.error.put("password", msg);
        } else if (!password.equals(password1)) {
            result.error.put("password1", "重复密码与原密码不一致");
        }
        msg = Validate.captcha(captcha, session, "验证码");
        if (msg != null) {
            result.error.put("captcha", msg);
        }

        if (result.error.size() > 0) {
            return result.toString();
        }

        msg = userService.register(name, password, Helper.getClientIp(request));

        if (msg == null) {
            Captcha.clear(session);
            result.setMsg("注册成功");
            //result.setUrl("/");
        } else {
            result.setMsg(msg);
            result.error.put("name", msg);
        }
        return result.toString();
    }

    @GetMapping(path = "/test", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getTest(HttpServletRequest request) {

        return Helper.getJson(request.getTrailerFields());
    }

}
