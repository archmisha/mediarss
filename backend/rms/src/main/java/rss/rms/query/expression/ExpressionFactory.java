package rss.rms.query.expression;

/**
 * Created with IntelliJ IDEA.
 * User: bramnik
 * Date: 20/05/13
 * Time: 16:14
 * To change this template use File | Settings | File Templates.
 */
public class ExpressionFactory {

    public static LTExpression createLTExpression(String fieldPath, Object value) {
        return new LTExpressionImpl(fieldPath, value);
    }

    public static GTExpression createGTExpression(String fieldPath, Object value) {
        return new GTExpressionImpl(fieldPath, value);
    }

    public static LTEExpression createLTEExpression(String fieldPath, Object value) {
        return new LTEExpressionImpl(fieldPath, value);
    }

    public static GTEExpression createGTEExpression(String fieldPath, Object value) {
        return new GTEExpressionImpl(fieldPath, value);
    }

    public static EqExpression createEQExpression(String fieldPath, Object value) {
        return new EqExpressionImpl(fieldPath, value);
    }

    public static NotEqExpression createNotEqExpression(String fieldPath, Object value) {
        return new NotEqExpressionImpl(fieldPath, value);
    }

    public static InExpression createInExpression(String fieldPath, Object... value) {
        return new InExpressionImpl(fieldPath, false, value);
    }

    public static InExpression createNotInExpression(String fieldPath, Object... value) {
        return new InExpressionImpl(fieldPath, true, value);
    }

    public static ExistsExpression createExistsExpression(String fieldPath) {
        return new ExistsExpressionImpl(fieldPath, false);
    }

    public static ExistsExpression createNotExistsExpression(String fieldPath) {
        return new ExistsExpressionImpl(fieldPath, true);
    }

    public static BetweenExpression createBetweenExpression(String fieldPath, Object leftBoundValue, Object rightBoundValue) {
        return new BetweenExpressionImpl(fieldPath, leftBoundValue, rightBoundValue, false);
    }

    public static BetweenExpression createNotBetweenExpression(String fieldPath, Object leftBoundValue, Object rightBoundValue) {
        return new BetweenExpressionImpl(fieldPath, leftBoundValue, rightBoundValue, true);
    }

    public static StartsWithExpression createStartsWithExpression(String fieldPath, String value) {
        return new StartsWithExpressionImpl(fieldPath, value);
    }

    public static AndLogicalExpressionSupport createAndExpression() {
        return new AndExpressionImpl();
    }

    public static OrLogicalExpressionSupport createOrExpression() {
        return new OrExpressionImpl();
    }
}
