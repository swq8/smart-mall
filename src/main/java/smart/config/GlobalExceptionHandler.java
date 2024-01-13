package smart.config;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.util.Strings;
import org.apache.tomcat.util.http.fileupload.InvalidFileNameException;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import smart.auth.AdminAuthException;
import smart.auth.UserAuthException;
import smart.lib.ApiJsonResult;
import smart.util.Json;
import smart.lib.JsonResult;
import smart.util.Helper;
import smart.util.LogUtils;

import java.io.IOException;


@ControllerAdvice
public class GlobalExceptionHandler {

    @Resource
    private AppProperties appProperties;

    private ResponseEntity<String> getResponseEntity(ApiJsonResult apiJsonResult) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(
                Json.stringify(apiJsonResult),
                httpHeaders,
                HttpStatus.OK.value());
    }


    /**
     * generate error response entity
     *
     * @return response entity
     */
    private ResponseEntity<String> getResponseEntity(HttpServletRequest request, HttpStatus httpStatus) {
        return getResponseEntity(request, httpStatus, null);
    }

    private ResponseEntity<String> getResponseEntity(HttpServletRequest request, HttpStatus httpStatus, String errorDetail) {
        if (!appProperties.isDevMode()) errorDetail = null;
        HttpHeaders httpHeaders = new HttpHeaders();
        String content;
        int httpStatusCode = httpStatus.value();
        String contentType = request.getHeader("content-type");
        if (contentType != null && contentType.startsWith("application/json")) {
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            ApiJsonResult apiJsonResult = new ApiJsonResult();
            apiJsonResult.setMsg(httpStatus.getReasonPhrase());
            if (HttpStatus.BAD_REQUEST.equals(httpStatus)) {
                apiJsonResult.setCode(ApiJsonResult.CODE_BAD_REQUEST);
                httpStatusCode = HttpStatus.OK.value();
            } else apiJsonResult.setCode(ApiJsonResult.CODE_ERROR);
            if (!Strings.isEmpty(errorDetail)) apiJsonResult.setMsg(apiJsonResult.getMsg() + ". " + errorDetail);
            content = Json.stringify(apiJsonResult);
        } else {
            httpHeaders.setContentType(MediaType.TEXT_HTML);
            content = Helper.getErrorHtml(httpStatus, errorDetail, Helper.getThemeName(request));
        }
        return new ResponseEntity<>(
                content,
                httpHeaders,
                httpStatusCode);
    }

    /**
     * 管理员鉴权异常
     *
     * @param request request
     * @param e       exception
     * @return 异常处理结果
     */
    @ExceptionHandler(AdminAuthException.class)
    public ResponseEntity<String> adminAuthExceptionHandler(HttpServletRequest request, AdminAuthException e) {
        ApiJsonResult apiJsonResult = new ApiJsonResult();
        if (e.isUnauthorized()) apiJsonResult.setCode(ApiJsonResult.CODE_UNAUTHORIZED).setMsg("无此权限");
        else apiJsonResult.setCode(ApiJsonResult.CODE_FORBIDDEN).setMsg("请先登录");
        return getResponseEntity(apiJsonResult);
    }

    /**
     * request data transfer exception
     *
     * @param request request
     * @return msg
     */
    @ExceptionHandler({HttpMessageNotReadableException.class})
    @ResponseBody
    public ResponseEntity<String> httpMessageNotReadableExceptionHandler(HttpServletRequest request, HttpMessageNotReadableException ex) {
        if (appProperties.isDevMode()) LogUtils.warn(ex.getClass(), ex);
        return getResponseEntity(request, HttpStatus.BAD_REQUEST, ex.getLocalizedMessage());
    }

    /**
     * HttpRequestMethodNotSupportedException
     *
     * @return METHOD_NOT_ALLOWED info
     */
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    @ResponseBody
    public ResponseEntity<String> httpRequestMethodNotSupportedExceptionHandler(HttpServletRequest request) {
        return getResponseEntity(request, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * 上传文件名包含\u0000等字符串时会引发该错误
     *
     * @return 400 error info
     */
    @ExceptionHandler(InvalidFileNameException.class)
    @ResponseBody
    public ResponseEntity<String> invalidFileNameExceptionExceptionHandler(HttpServletRequest request) {
        return getResponseEntity(request, HttpStatus.BAD_REQUEST);
    }

    /**
     * request data transfer exception with
     *
     * @param ex exception
     * @return msg
     */
    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseBody
    public ResponseEntity<String> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException ex) {
        if (appProperties.isDevMode()) LogUtils.warn(ex.getClass(), ex);
        return getResponseEntity(ApiJsonResult.badRequest(ex.getBindingResult()));
    }

    /**
     * 上传文件过大时会引发该错误
     *
     * @return 413 error info
     */
    @ExceptionHandler(SizeLimitExceededException.class)
    @ResponseBody
    public ResponseEntity<String> sizeLimitExceededExceptionExceptionHandler(HttpServletRequest request) {
        return getResponseEntity(request, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler(UserAuthException.class)
    public void userAuthExceptionHandler(HttpServletRequest request, HttpServletResponse response, UserAuthException e) {
        String loginUri = "/user/login";
        if (request.getMethod().equals("GET")) {
            loginUri += "?back=" + Helper.urlEncode(request.getRequestURI());
            String queryString = request.getQueryString();
            if (queryString != null) {
                loginUri += Helper.urlEncode('?' + queryString);
            }
            try {
                response.sendRedirect(loginUri);
            } catch (IOException ex) {
                LogUtils.error(getClass(), "", ex);
            }
        } else {
            JsonResult jsonResult = new JsonResult();
            jsonResult.setUrl(loginUri);
            try {
                response.getWriter().write(jsonResult.toString());
                response.getWriter().write(jsonResult.toString());
            } catch (IOException ex) {
                LogUtils.error(getClass(), "", ex);
            }
        }
    }
}
