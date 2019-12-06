package gaarason.database.contracts.builder;

import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.query.Builder;

public interface Union<T>{

    Builder<T> union(GenerateSqlPart<T> closure);

    Builder<T> unionAll(GenerateSqlPart<T> closure);
}
