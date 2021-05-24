package gaarason.database.contract.eloquent;

import gaarason.database.contract.builder.*;
import gaarason.database.contract.query.Grammar;

/**
 * SQL组装
 * @param <T> 实体类
 * @param <K> 主键类型
 */
public interface Builder<T, K> extends Cloneable, Where<T, K>, Having<T, K>, Union<T, K>, Support<T, K>,
    From<T, K>, Execute<T, K>, With<T, K>, Select<T, K>, OrderBy<T, K>, Limit<T, K>, Group<T, K>, Value<T, K>,
    Data<T, K>, Transaction<T, K>, Aggregates<T, K>, Paginator<T, K>, Index<T, K>, Lock<T, K>, Native<T, K>, Join<T, K>,
    Ability<T, K> {

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
