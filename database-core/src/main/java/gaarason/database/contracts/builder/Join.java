package gaarason.database.contracts.builder;

import gaarason.database.query.Builder;

public interface Join<T> {

    Builder<T> join(String table, String column1, String symbol, String column2);

    Builder<T> join(gaarason.database.eloquent.JoinType joinType, String table, String column1, String symbol,
                    String column2);

}
