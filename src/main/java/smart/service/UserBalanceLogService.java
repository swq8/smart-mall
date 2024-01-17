package smart.service;

import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import smart.lib.Pagination;
import smart.util.Helper;
import smart.util.SqlBuilder;
import smart.dto.GeneralQueryDto;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserBalanceLogService {

    static String[] SORTABLE_COLUMNS = new String[]{
            "id", "amount", "balance", "time"
    };

    public Pagination getListByUid(long uid, HttpServletRequest request) {
        String sql = "select * from t_user_balance_log where uid = ? order by time desc";
        var pagination = Pagination.newBuilder(sql, new Object[]{uid})
                .page(request)
                .build();
        return formatPagination(pagination);
    }

    @NotNull
    private Pagination formatPagination(Pagination pagination) {
        pagination.getRows().forEach(row -> {
            var change = Helper.parseNumber(row.get("amount"), Long.class);
            String changeStr = Helper.priceFormat(change);
            if (change > 0) {
                changeStr = "+" + changeStr;
            }
            row.put("amountStr", changeStr);
            row.put("balanceStr", Helper.priceFormat(Helper.parseNumber(row.get("balance"), Long.class)));
        });
        return pagination;
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
        var pagination = Pagination.newBuilder(sql, sqlParams.toArray())
                .page(query.getPage())
                .pageSize(query.getPageSize())
                .build();
        return formatPagination(pagination);
    }
}
