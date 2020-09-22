package gaarason.database.spring.boot.starter.configurations;

import gaarason.database.contract.connection.GaarasonDataSource;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import javax.annotation.Resource;

public class GaarasonTransactionManagerConfiguration extends AbstractPlatformTransactionManager {

    @Resource
    GaarasonDataSource gaarasonDataSource;

    @Override
    protected Object doGetTransaction() throws TransactionException {
        return null;
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
}
