package gaarason.database.spring.boot.starter.configurations;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.eloquent.GeneralModel;
import gaarason.database.generator.GeneralGenerator;
import gaarason.database.spring.boot.starter.provider.SavepointManagerProvider;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import javax.annotation.Resource;

@Import({SavepointManagerProvider.class})
public class GaarasonTransactionManagerConfiguration extends AbstractPlatformTransactionManager {

    @Resource
    GaarasonDataSource gaarasonDataSource;

    @Resource
    SavepointManagerProvider savepointManagerProvider;

    public GaarasonTransactionManagerConfiguration(){
        setNestedTransactionAllowed(true);
    }

    @Override
    protected Object doGetTransaction() throws TransactionException {
        return savepointManagerProvider;
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
}
