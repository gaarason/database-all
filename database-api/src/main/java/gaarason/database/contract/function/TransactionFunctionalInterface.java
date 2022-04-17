package gaarason.database.contract.function;

@FunctionalInterface
public interface TransactionFunctionalInterface<V> {

    /**
     * 事物处理
     */
    V execute() ;
}
