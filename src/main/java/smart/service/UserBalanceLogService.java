package smart.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import smart.dto.GeneralQueryDto;
import smart.lib.Pagination;
import smart.util.SqlBuilder;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserBalanceLogService {

    static String[] SORTABLE_COLUMNS = new String[]{
            "id", "amount", "balance", "time"
    };

    public Pagination getListByUid(long uid, HttpServletRequest request) {
        String sql = "select * from t_user_balance_log where uid = ? order by time desc";
        return Pagination.newBuilder(sql, new Object[]{uid})
                .page(request)
                .build();
    }

    public Pagination query(GeneralQueryDto query) {
        String sql = """
                select t1.id,
                    date_format(t1.time, '%Y-%m-%d %T') as time,
                       t2.name,
                       t1.amount,
                       t1.balance,
                       t1.note
                from t_user_balance_log t1
                         left join t_user t2 on t1.uid = t2.id
                """;
        List<Object> sqlParams = new ArrayList<>();
        sql = new SqlBuilder(sql, sqlParams)
                .andEqualsIfNotNull("t1.uid", query.getUid())
                .andTrimEqualsIfNotBlank("name", query.getName())
                .orderBy(SORTABLE_COLUMNS, query.getSort(), "time,desc")
                .buildSql();
        return Pagination.newBuilder(sql, sqlParams.toArray())
                .page(query.getPage())
                .pageSize(query.getPageSize())
                .build();
    }
}
