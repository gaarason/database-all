package gaarason.database.spring.boot.starter.provider;

import gaarason.database.contract.connection.GaarasonDataSource;
import org.springframework.transaction.SavepointManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

public class GaarasonTransactionManager extends AbstractPlatformTransactionManager implements SavepointManager {

    protected GaarasonDataSource gaarasonDataSource;

    public GaarasonTransactionManager(GaarasonDataSource gaarasonDataSource) {
        this.gaarasonDataSource = gaarasonDataSource;
        setNestedTransactionAllowed(true);
    }

    @Override
    protected Object doGetTransaction() throws TransactionException {
        return this;
    }

    @Override
    protected boolean isExistingTransaction(Object transaction) throws TransactionException {
        return gaarasonDataSource.isLocalThreadInTransaction();
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
        gaarasonDataSource.begin();
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
        gaarasonDataSource.commit();
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
        gaarasonDataSource.rollBack();
    }

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
