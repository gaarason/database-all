package gaarason.database.contracts.builder;

import gaarason.database.query.Builder;

public interface From<T> {
    Builder<T> from(String table);
}
