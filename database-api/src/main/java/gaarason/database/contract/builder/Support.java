package gaarason.database.contract.builder;

import gaarason.database.appointment.SqlType;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.function.BuilderAnyWrapper;
import gaarason.database.contract.function.BuilderWrapper;
import gaarason.database.contract.query.Grammar;
import gaarason.database.contract.support.LambdaStyle;
import gaarason.database.contract.support.ShowType;
import gaarason.database.exception.CloneNotSupportedRuntimeException;
import gaarason.database.lang.Nullable;

/**
 * 支持
 * @author xt
 */
public interface Support<B extends Builder<B, T, K>, T, K> extends LambdaStyle, Cloneable{

    /**
     * 构造函数
     * @param gaarasonDataSource 数据源
     * @param model 数据模型
     * @param grammar 语法
     * @return 查询构造器
     */
    B initBuilder(GaarasonDataSource gaarasonDataSource, Model<T, K> model, Grammar grammar);

    /**
     * sql生成器
     * @return sql生成器
     */
    Grammar getGrammar();

    /**
     * 覆盖 语法分析
     * @param grammar 语法分析
     */
    void setGrammar(Grammar grammar);

    /**
     * 合并 语法分析
     * @param grammar 语法分析
     */
    void mergerGrammar(Grammar grammar);

    /**
     * 得到一个全新的查询构造器
     * @return 查询构造器
     */
    B getNewSelf();

    /**
     * 返回当前的查询构造器
     * @return 查询构造器
     */
    B getSelf();

    /**
     * 类型显示
     * @param builderClass 子类
     * @return 查询构造器
     * @param <BB> 子类类形
     */
    default  <BB extends Builder<BB, TT, KK>, TT, KK> BB showType(ShowType<BB> builderClass) {
        return (BB) getSelf();
    }

    /**
     * 清除指定的sql片段
     * @param sqlPartType SQL片段
     * @return 查询构造器
     */
    B clear(Grammar.SQLPartType sqlPartType);

    /**
     * 复制当前的查询构造器
     * 相互不存在引用 即深拷贝实现
     * @return 查询构造器
     * @throws CloneNotSupportedRuntimeException 克隆出错
     */
    B clone() throws CloneNotSupportedRuntimeException;

    /**
     * 覆盖 查询构造器
     * @param builder 查询构造器
     * @return 查询构造器
     */
    default B setAnyBuilder(Builder<?, ?, ?> builder) {
        setGrammar(builder.getGrammar().deepCopy());
        return getSelf();
    }

    /**
     * 覆盖 查询构造器
     * @param builder 查询构造器
     * @return 查询构造器
     */
    default B setBuilder(B builder) {
        return setAnyBuilder(builder);
    }

    /**
     * 合并 查询构造器
     * @param builder 查询构造器
     * @return 查询构造器
     */
    default B mergerAnyBuilder(Builder<?, ?, ?> builder) {
        mergerGrammar(builder.getGrammar().deepCopy());
        return getSelf();
    }

    /**
     * 合并 查询构造器
     * @param builder 查询构造器
     * @return 查询构造器
     */
    default B mergerBuilder(B builder) {
        return mergerAnyBuilder(builder);
    }

    /**
     * 执行闭包生成完整sql, 含绑定参数的合并
     * @param closure 闭包
     * @return sql
     */
    default Grammar.SQLPartInfo generateSql(BuilderWrapper<B, T, K> closure) {
        B subBuilder = closure.execute(getNewSelf());
        return subBuilder.getGrammar().generateSql(SqlType.SELECT);
    }

    /**
     * 执行闭包生成完整sql, 含绑定参数的合并
     * @param closure 闭包
     * @return sql
     */
    default Grammar.SQLPartInfo generateSql(BuilderAnyWrapper closure) {
        Builder<?, ?, ?> subBuilder = closure.execute(getNewSelf());
        return subBuilder.getGrammar().generateSql(SqlType.SELECT);
    }

    /**
     * 执行闭包生成sql片段, 含绑定参数的合并
     * @param closure 闭包
     * @param sqlPartType 片段类型
     * @return sql
     */
    default Grammar.SQLPartInfo generateSql(BuilderWrapper<B, T, K> closure,
        Grammar.SQLPartType sqlPartType) {
        B subBuilder = closure.execute(getNewSelf());
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

}
