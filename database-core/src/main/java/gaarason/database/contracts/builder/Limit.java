package gaarason.database.contracts.builder;

import gaarason.database.query.Builder;

public interface Limit<T> {

    Builder<T> limit(int offset, int take);

    Builder<T> limit(int take);


}
