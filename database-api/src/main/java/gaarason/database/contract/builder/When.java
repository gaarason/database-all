package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;

import java.io.Serializable;

/**
 * 条件子句
 * @param <T>
 * @param <K>
 * @author xt
 */
public interface When<T extends Serializable, K extends Serializable> {

    /**
     * 当condition==true时, 执行closure
     * @param condition 条件
     * @param closure 查询构造
     * @return 查询构造器
     */
    Builder<T, K> when(boolean condition, GenerateSqlPartFunctionalInterface<T, K> closure);

    /**
     * 当condition==true时, 执行closureIfTrue, 反之执行closureIfFalse
     * @param condition 条件
     * @param closureIfTrue 查询构造
     * @param closureIfFalse 查询构造
     * @return 查询构造器
     */
    Builder<T, K> when(boolean condition, GenerateSqlPartFunctionalInterface<T, K> closureIfTrue, GenerateSqlPartFunctionalInterface<T, K> closureIfFalse);


}
