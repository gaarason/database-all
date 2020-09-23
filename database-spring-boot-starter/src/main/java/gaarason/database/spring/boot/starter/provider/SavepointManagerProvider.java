package gaarason.database.spring.boot.starter.provider;

import gaarason.database.contract.connection.GaarasonDataSource;
import org.springframework.transaction.SavepointManager;
import org.springframework.transaction.TransactionException;

import javax.annotation.Resource;

public class SavepointManagerProvider implements SavepointManager {

    @Resource
    GaarasonDataSource gaarasonDataSource;

    @Override
    public Object createSavepoint() throws TransactionException {
        return gaarasonDataSource.createSavepoint();
    }

    @Override
    public void rollbackToSavepoint(Object savepoint) throws TransactionException {
        gaarasonDataSource.rollbackToSavepoint();
    }

    @Override
    public void releaseSavepoint(Object savepoint) throws TransactionException {
        gaarasonDataSource.releaseSavepoint();
    }
}
