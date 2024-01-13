package smart.controller.user;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import smart.auth.UserToken;
import smart.cache.SystemCache;
import smart.entity.UserEntity;
import smart.lib.Pagination;
import smart.service.UserBalanceLogService;
import smart.util.DbUtils;
import smart.util.Helper;

@Controller
@RequestMapping(path = "user/balance")
@Transactional
public class Balance {
    @Resource
    UserBalanceLogService userBalanceLogService;


    @GetMapping(path = "")
    public ModelAndView getIndex(HttpServletRequest request, UserToken userToken) {
        var userEntity = DbUtils.findById(userToken.getId(), UserEntity.class);
        ModelAndView view = Helper.newModelAndView("user/balance/index", request);
        Pagination pagination = userBalanceLogService.getListByUid(userToken.getId(), request);
        view.addObject("balance", userEntity.getBalance());
        view.addObject("pagination", pagination);
        view.addObject("title", SystemCache.getSiteName() + " - 余额");
        return view;
    }
}
