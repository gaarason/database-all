package gaarason.database.contracts.builder;

import gaarason.database.core.lang.Nullable;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.RecordList;
import gaarason.database.exception.EntityNotFoundException;
import gaarason.database.exception.SQLRuntimeException;

import java.util.Collection;

public interface Native<T> {

    Record<T> queryOrFail(String sql, Collection<String> parameters)
        throws SQLRuntimeException, EntityNotFoundException;

    @Nullable
    Record<T> query(String sql, Collection<String> parameters) throws SQLRuntimeException;

    RecordList<T> queryList(String sql, Collection<String> parameters) throws SQLRuntimeException;

    int execute(String sql, Collection<String> parameters) throws SQLRuntimeException;

}
