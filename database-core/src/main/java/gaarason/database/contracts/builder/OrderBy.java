package gaarason.database.contracts.builder;

import gaarason.database.query.Builder;

public interface OrderBy<T> {

    Builder<T> orderBy(String column, gaarason.database.eloquent.OrderBy orderByType);

    Builder<T> orderBy(String column);

}
