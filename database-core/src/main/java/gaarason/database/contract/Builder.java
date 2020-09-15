package gaarason.database.contract;

import gaarason.database.contract.builder.*;

public interface Builder<T, K> extends Cloneable, Where<T, K>, Having<T, K>, Union<T, K>, Support<T, K>,
    From<T, K>, Execute<T, K>, With<T, K>, Select<T, K>, OrderBy<T, K>, Limit<T, K>, Group<T, K>, Value<T, K>,
    Data<T, K>, Transaction<T, K>, Aggregates<T, K>, Paginator<T, K>, Lock<T, K>, Native<T, K>, Join<T, K>,
    Ability<T, K> {
}
