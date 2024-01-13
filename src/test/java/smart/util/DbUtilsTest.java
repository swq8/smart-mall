package smart.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import smart.entity.OrderGoodsEntity;

class DbUtilsTest {

    @Test
    void camelCaseToUnderscoresNaming() {
        Assertions.assertEquals(DbUtils.camelCaseToUnderscoresNaming("UserID"), "userid");
        Assertions.assertEquals(DbUtils.camelCaseToUnderscoresNaming("userId"), "user_id");
    }

    @Test
    void getTableNameByEntity() {
        Assertions.assertEquals(DbUtils.getTableName(OrderGoodsEntity.class), "t_order_goods");
    }

    @Test
    void underscoresToCamelCaseNaming() {
        Assertions.assertEquals(DbUtils.underscoresToCamelCaseNaming("ORDER_ID"), "orderId");
        Assertions.assertEquals(DbUtils.underscoresToCamelCaseNaming("user_id"), "userId");
    }
}