package gaarason.database.contract.eloquent;

import gaarason.database.contract.builder.*;
import gaarason.database.contract.query.Grammar;

import java.io.Serializable;

/**
 * SQL组装
 * @param <T> 实体类
 * @param <K> 主键类型
 * @author xt
 */
public interface Builder<T extends Serializable, K extends Serializable> extends Cloneable, Debug, Column<T, K>, Where<T, K>,
    Having<T, K>, Union<T, K>, Support, From<T, K>, Execute<T, K>, With<T, K>, Select<T, K>, Order<T, K>, Limit<T, K>,
    Group<T, K>, Value<T, K>, Data<T, K>, Transaction, Aggregates, Pager<T>, Index<T, K>, Lock<T, K>, Native<T, K>,
    Join<T, K>, Ability<T, K>, When<T, K> {

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
}
