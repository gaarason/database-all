package gaarason.database.eloquent.repository;

import gaarason.database.eloquent.Record;
import gaarason.database.exception.MethodNotSupportedException;

abstract public class BaseShortcut<T, K> extends BaseSoftDeleting<T, K> {

    protected ThreadLocal<Record<T, K>> newRecordForShortcut = new ThreadLocal<>();

    @Override
    public T getEntity() {
        Record<T, K> newRecord = newRecord();
        newRecordForShortcut.set(newRecord);
        return newRecord.getEntity();
    }

    @Override
    public boolean save() {
        try {
            return newRecordForShortcut.get().save();
        } finally {
            newRecordForShortcut.remove();
        }
    }

    @Override
    public boolean delete() {
        throw new MethodNotSupportedException();
    }

    @Override
    public boolean restore() {
        throw new MethodNotSupportedException();
    }

    @Override
    public boolean restore(boolean refresh) {
        throw new MethodNotSupportedException();
    }

    @Override
    public Record<T, K> refresh() {
        throw new MethodNotSupportedException();
    }
}
