package gaarason.database.contracts.builder;

import gaarason.database.query.Builder;

import java.util.List;

public interface Value<T> {

    Builder<T> value(List<String> valueList);

    Builder<T> valueList(List<List<String>> valueList);



}
