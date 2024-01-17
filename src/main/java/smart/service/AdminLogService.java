package smart.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import smart.auth.UserToken;
import smart.dto.GeneralQueryDto;
import smart.entity.AdminLogEntity;
import smart.lib.ApiJsonResult;
import smart.lib.Pagination;
import smart.util.DbUtils;
import smart.util.Helper;
import smart.util.Json;
import smart.util.SqlBuilder;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminLogService {
    public void addLog(HttpServletRequest request, String title, Object content) {
        UserToken userToken = (UserToken) request.getAttribute(StringUtils.uncapitalize(UserToken.class.getSimpleName()));
        long runTime = System.currentTimeMillis() - (long) request.getAttribute("startTime");
        String str;
        if (content == null) {
            str = "";
        } else if (content instanceof String) {
            str = (String) content;
        } else {
            str = Json.stringify(content);
        }
        addLog(userToken.getId(), Helper.getClientIp(request), title, str, (int) runTime);

    }

    public void addLog(long uid, String ip, String title, String content, int runtime) {
        AdminLogEntity adminLogEntity = new AdminLogEntity();
        adminLogEntity.setUid(uid);
        adminLogEntity.setIp(ip);
        adminLogEntity.setTitle(title);
        adminLogEntity.setContent(content);
        adminLogEntity.setRuntime(runtime);
        DbUtils.insert(adminLogEntity);
    }

    public void addLogIfSuccess(ApiJsonResult apiJsonResult, HttpServletRequest request, String title, Object content) {
        if (apiJsonResult.getCode() == ApiJsonResult.CODE_SUCCESS) {
            addLog(request, title, content);
        }
    }

    public Pagination query(GeneralQueryDto query) {
        String[] sortableColumns = new String[]{"id", "runtime"};
        List<Object> sqlParams = new ArrayList<>();
        String sql =
                """
                        select t1.id,
                               date_format(t1.time, '%Y-%m-%d %T') as time,
                               t1.uid,
                               t2.name,
                               t1.ip,
                               t1.title,
                               t1.content,
                               t1.runtime
                        from t_admin_log t1
                                 left join t_user t2 on t1.uid = t2.id
                        """;
        SqlBuilder sqlBuilder = new SqlBuilder(sql, sqlParams)
                .andLikeIfNotBlank("ip", query.getIp())
                .andLikeIfNotBlank("name", query.getName())
                .andLikeIfNotBlank("title", query.getQ())
                .orderBy(sortableColumns, query.getSort(), "id,desc");


        return Pagination.newBuilder(sqlBuilder.buildSql(), sqlParams.toArray())
                .page(query.getPage())
                .pageSize(query.getPageSize())
                .build();
    }
}
