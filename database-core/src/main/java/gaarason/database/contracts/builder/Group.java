package gaarason.database.contracts.builder;

import gaarason.database.query.Builder;

import java.util.List;

public interface Group<T> {

    Builder<T> groupRaw(String sqlPart);

    Builder<T> group(String column);

    Builder<T> group(String... column);

    Builder<T> group(List<String> columnList);

}
