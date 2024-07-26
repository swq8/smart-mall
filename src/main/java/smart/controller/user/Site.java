package smart.controller.user;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import smart.auth.UserToken;
import smart.cache.SystemCache;
import smart.config.AppConfig;
import smart.entity.UserEntity;
import smart.lib.Captcha;
import smart.lib.Cart;
import smart.lib.JsonResult;
import smart.lib.session.Session;
import smart.repository.UserRepository;
import smart.service.UserService;
import smart.util.DbUtils;
import smart.util.Helper;
import smart.util.Json;
import smart.util.ValidatorUtils;

@Controller(value = "user/site")
@RequestMapping(path = "user")
@Transactional
public class Site {

    @Resource
    PasswordEncoder passwordEncoder;

    @Resource
    UserRepository userRepository;

    @Resource
    UserService userService;


    @GetMapping(path = "central")
    public ModelAndView getCentral(HttpServletRequest request, UserToken userToken) {
        ModelAndView modelAndView = Helper.newModelAndView("user/central", request);
        UserEntity userEntity = userRepository.findById(userToken.getId()).orElse(null);
        modelAndView.addObject("title", SystemCache.getSiteName() + " - 用户中心");
        modelAndView.addObject("userEntity", userEntity);
        return modelAndView;
    }

    @GetMapping(path = "info")
    public ModelAndView getInfo(HttpServletRequest request, UserToken userToken) {
        ModelAndView modelAndView = Helper.newModelAndView("user/info", request);
        UserEntity userEntity = userRepository.findById(userToken.getId()).orElse(null);
        modelAndView.addObject("title", SystemCache.getSiteName() + " - 个人信息");
        modelAndView.addObject("userEntity", userEntity);
        return modelAndView;
    }


    @PostMapping(path = "info", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postInfo(HttpServletRequest request, Session session, UserToken userToken, @RequestParam(required = false) String phone) {
        String msg;
        JsonResult jsonResult = new JsonResult();
        msg = ValidatorUtils.validateMobile(phone, "手机号");
        if (msg != null) {
            jsonResult.error.put("phone", msg);
            return jsonResult.toString();
        }
        if (jsonResult.error.isEmpty()) {
            UserEntity userEntity = DbUtils.findByIdForWrite(userToken.getId(), UserEntity.class);
            if (userEntity == null) {
                jsonResult.setMsg("修改失败,请刷新重试");
                return jsonResult.toString();
            } else {
                userEntity.setPhone(phone);
                userRepository.saveAndFlush(userEntity);
                jsonResult.setMsg("修改成功");
                jsonResult.setUrl("/user/central");
                userToken.setPhone(phone);
                userToken.save(session);
            }
        }
        return Helper.msgPage(jsonResult, request);
    }

    @GetMapping(path = "login")
    public ModelAndView getLogin(HttpServletRequest request) {
        if (request.getParameter("logout") != null) {
            userService.logout(request);
        }
        ModelAndView modelAndView = Helper.newModelAndView("user/login", request);
        modelAndView.addObject("title", SystemCache.getSiteName() + " - 用户登录");
        modelAndView.addObject("back", request.getParameter("back"));
        return modelAndView;
    }

    @PostMapping(path = "login", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postLogin(HttpServletRequest request, Session session,
                            @RequestParam(defaultValue = "") String back,
                            @RequestParam(defaultValue = "") String name,
                            @RequestParam(defaultValue = "") String password
    ) {
        if (!StringUtils.hasText(back)) {
            back = "/";
        }
        name = name.toLowerCase();
        var oldCart = new Cart(request);
        JsonResult jsonResult = new JsonResult();
        UserService.Result result = userService.login(name, password, Helper.getClientIp(request));
        if (result.userEntity != null) {
            //login success
            loginInit(request, result.getUserEntity(), session);
            jsonResult.setUrl(back);

        } else {
            jsonResult.setMsg(result.error);
            jsonResult.error.putAll(result.errors);
        }
        return jsonResult.toString();

    }

    @GetMapping(path = "password")
    public ModelAndView getPassword(HttpServletRequest request) {
        ModelAndView modelAndView = Helper.newModelAndView("user/password", request);
        modelAndView.addObject("title", SystemCache.getSiteName() + " - 修改密码");
        return modelAndView;
    }

    @PostMapping(path = "password", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postPassword(HttpServletRequest request, UserToken userToken,
                               @RequestParam(defaultValue = "") String oldPassword,
                               @RequestParam(defaultValue = "") String password,
                               @RequestParam(defaultValue = "") String password1) {

        String msg;
        JsonResult jsonResult = new JsonResult();
        msg = ValidatorUtils.validatePassword(password, "新密码");
        if (msg == null) {
            if (password.equals(oldPassword)) {
                jsonResult.error.put("password", "新旧密码不得相同");
            }
        } else {
            jsonResult.error.put("password", msg);
        }

        if (jsonResult.error.isEmpty() && !password.equals(password1)) {
            jsonResult.error.put("password1", "重复密码与新密码不一致");
        }
        var userEntity = DbUtils.findByIdForWrite(userToken.getId(), UserEntity.class);
        if (jsonResult.error.isEmpty()) {
            if (!passwordEncoder.matches(oldPassword, userEntity.getPassword())) {
                jsonResult.error.put("oldPassword", "原密码错误");
            }
        }
        if (jsonResult.error.isEmpty()) {
            UserService userService = AppConfig.getContext().getBean(UserService.class);
            String salt = userService.changePassword(userToken.getId(), password);
            userService.deleteSessionsByUserId(userEntity.getId());
            if (salt == null) {
                jsonResult.setMsg("密码修改成功").setUrl("/user/central");
            } else {
                jsonResult.setMsg("修改失败,请刷新重试  ");
            }
        }
        if (StringUtils.hasLength(jsonResult.getUrl())) {
            return Helper.msgPage(jsonResult, request);
        }
        return jsonResult.toString();
    }

    @GetMapping(path = "register")
    public ModelAndView getRegister(HttpServletRequest request) {
        var modelAndView = Helper.newModelAndView("user/register", request);
        modelAndView.addObject("title", SystemCache.getSiteName() + " - 注册新用户");
        return modelAndView;
    }

    @PostMapping(path = "register", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postRegister(HttpServletRequest request, Session session,
                               @RequestParam(defaultValue = "") String name,
                               @RequestParam(defaultValue = "") String password,
                               @RequestParam(defaultValue = "") String password1,
                               @RequestParam(defaultValue = "") String captcha) {

        name = name.toLowerCase();

        JsonResult result = new JsonResult();
        String msg = ValidatorUtils.validateName(name, "用户名");
        if (msg != null) {
            result.setMsg(msg).error.put("name", msg);
        }

        msg = ValidatorUtils.validatePassword(password, "密码");
        if (msg != null) {
            result.setMsg(msg).error.put("password", msg);
        } else if (!password.equals(password1)) {
            msg = "重复密码与原密码不一致";
            result.setMsg(msg).error.put("password1", msg);
        }
        msg = ValidatorUtils.validateCaptcha(captcha, session, "验证码");
        if (msg != null) {
            result.setMsg(msg).error.put("captcha", msg);
        }

        if (!result.error.isEmpty()) {
            return result.toString();
        }

        msg = userService.register(name, password, Helper.getClientIp(request));
        if (msg == null) {
            UserEntity userEntity = userRepository.findByName(name);
            loginInit(request, userEntity, session);
            result.setUrl("/user/central").setMsg("注册会员成功");
            return Helper.msgPage(result, request);
        } else {
            result.setMsg(msg).error.put("name", msg);
        }
        return result.toString();
    }

    @GetMapping(path = "/test", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getTest(HttpServletRequest request) {

        return Json.stringify(request.getTrailerFields());
    }

    /**
     * login init, temporary cart goods  transferred to the user's  cart
     *
     * @param request    http request
     * @param userEntity user entity
     * @param session    session
     */
    private void loginInit(HttpServletRequest request, UserEntity userEntity, Session session) {
        var oldCart = new Cart(request);
        UserToken userToken = new UserToken(userEntity);
        userToken.save(session);
        request.setAttribute(StringUtils.uncapitalize(UserToken.class.getSimpleName()), userToken);
        var cart = new Cart(request);
        if (oldCart.getUserToken() == null) {
            oldCart.getItems().forEach(cart::add);
            session.delete(Cart.NAME);
        }
        Captcha.clear(session);
    }

}
