package smart.controller.adminApi;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import smart.config.AppConfig;
import smart.lib.ApiJsonResult;
import smart.storage.Storage;
import smart.storage.UploadResult;
import smart.util.LogUtils;
import smart.util.Security;

import java.math.BigInteger;

public class General {
    final static String[] IMAGES_EXT = new String[]{
            "jpe", "jpeg", "jpg", "png", "webp"
    };

    public static String checkFile(MultipartFile file, int sizeLimit, boolean imgOnly) {
        if (file == null || file.isEmpty() || !StringUtils.hasText(file.getOriginalFilename())) return "未找到上传文件";
        if (sizeLimit > 0 && file.getSize() > sizeLimit) return "文件体积太大";
        if (imgOnly) {
            String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
            if (StringUtils.hasLength(ext)) ext = ext.toLowerCase();
            if (!ObjectUtils.containsElement(IMAGES_EXT, ext))
                return file.getOriginalFilename() + " 不是图片文件";
        }
        return null;
    }


    /**
     * use checkFile method before this method
     *
     * @param request request
     * @param file    file
     * @return result
     */
    public static ApiJsonResult upload(HttpServletRequest request, MultipartFile file) {
        String fileName = file.getOriginalFilename();
        assert fileName != null;
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (StringUtils.hasText(ext)) {
            if (ext.equals("jpe") || ext.equals("jpeg")) {
                ext = "jpg";
            }
            ext = "." + ext;
        }
        try {
            String uri = Security.sha3_256(file.getInputStream()) + Long.toHexString(file.getSize());
            uri = new BigInteger(uri, 16).toString(36);
            uri = uri.substring(0, 2) + "/" + uri.substring(2, 4) + "/" + uri.substring(4);

            /* whether keep original filename */
            if (request.getParameter("keep") == null) uri += ext;
            else uri += "/" + file.getOriginalFilename();


            ApiJsonResult result = new ApiJsonResult();
            try (Storage storage = AppConfig.getContext().getBean(Storage.class)) {
                UploadResult uploadResult = storage.upload(file.getInputStream(), uri);
                if (uploadResult.getErr() == null) {
                    result.setCode(ApiJsonResult.CODE_SUCCESS);
                    result.putDataItem("url", uploadResult.getUrl());
                } else {
                    result.setCode(ApiJsonResult.CODE_ERROR);
                    result.setMsg(uploadResult.getErr());
                }
            }
            file.getInputStream().close();
            return result;
        } catch (Exception ex) {
            LogUtils.error(General.class, ex);
            return ApiJsonResult.error("系统错误");
        }
    }
}
