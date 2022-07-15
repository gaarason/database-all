package gaarason.database.contract.builder;

import gaarason.database.appointment.SqlType;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.contract.query.Grammar;
import gaarason.database.contract.support.LambdaStyle;
import gaarason.database.lang.Nullable;

/**
 * 支持
 * @author xt
 */
public interface Support<T, K> extends LambdaStyle<T, K> {

    /**
     * sql生成器
     * @return sql生成器
     */
    Grammar getGrammar();

    /**
     * sql生成器
     * @param grammar sql生成器
     */
    void setGrammar(Grammar grammar);

    /**
     * 得到一个全新的查询构造器
     * @return 查询构造器
     */
    Builder<T, K> getNewSelf();

    /**
     * 返回当前的查询构造器
     * @return 查询构造器
     */
    Builder<T, K> getSelf();

    /**
     * 执行闭包生成完整sql, 含绑定参数的合并
     * @param closure 闭包
     * @return sql
     */
    default Grammar.SQLPartInfo generateSql(GenerateSqlPartFunctionalInterface<T, K> closure) {
        Builder<T, K> subBuilder = closure.execute(getNewSelf());
        return subBuilder.getGrammar().generateSql(SqlType.SELECT);
    }

    /**
     * 执行闭包生成sql片段, 含绑定参数的合并
     * @param closure 闭包
     * @param sqlPartType 片段类型
     * @return sql
     */
    default Grammar.SQLPartInfo generateSql(GenerateSqlPartFunctionalInterface<T, K> closure,
        Grammar.SQLPartType sqlPartType) {
        Builder<T, K> subBuilder = closure.execute(getNewSelf());
        return subBuilder.getGrammar().get(sqlPartType);
    }

    /**
     * 类型转化到 String集合
     * @param value 参数
     * @return String
     */
    @Nullable
    String conversionToString(@Nullable Object value);

    /**
     * 类型转化到 int
     * null -> 0
     * @param value 参数
     * @return int
     */
    int conversionToInt(@Nullable Object value);

    /**
     * 类型转化到 String集合
     * @param value 参数
     * @return String集合
     */
//    @Nullable
//    default Collection<String> conversionToStrings(@Nullable Collection<?> value) {
//        if (value != null && !value.isEmpty()) {
//            LinkedList<String> res = new LinkedList<>();
//            for (Object obj : value) {
//                res.add(conversionToString(obj));
//            }
//            return res;
//        }
//        return null;
//    }
}
