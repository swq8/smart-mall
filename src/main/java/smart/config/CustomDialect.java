package smart.config;

import smart.lib.thymeleaf.HelperUtils;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;

import java.util.Collections;
import java.util.Set;

/**
 * thymeleaf模板中使用自定义方言
 */
public class CustomDialect extends AbstractDialect implements IExpressionObjectDialect {

    public CustomDialect() {
        super("Custom Dialect");
    }

    @Override
    public IExpressionObjectFactory getExpressionObjectFactory() {

        return new IExpressionObjectFactory() {

            @Override
            public Set<String> getAllExpressionObjectNames() {
                return Collections.singleton("helpers");
            }

            @Override
            public Object buildObject(IExpressionContext iExpressionContext, String s) {
                return HelperUtils.class;
            }


            @Override
            public boolean isCacheable(String expressionObjectName) {
                return true;
            }
        };

    }
}
