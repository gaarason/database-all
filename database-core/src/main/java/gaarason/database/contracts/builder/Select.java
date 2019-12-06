package gaarason.database.contracts.builder;

import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.core.lang.Nullable;
import gaarason.database.query.Builder;

import java.util.List;

public interface Select<T> {

    Builder<T> select(String column);

    Builder<T> select(String... column);

    Builder<T> select(List<String> columnList);

    Builder<T> selectFunction(String function, String parameter, @Nullable String alias);

    Builder<T> selectFunction(String function, GenerateSqlPart<T> closure, @Nullable String alias);

}
