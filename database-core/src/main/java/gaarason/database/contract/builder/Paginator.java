package gaarason.database.contract.builder;

import gaarason.database.eloquent.Paginate;
import gaarason.database.exception.SQLRuntimeException;

/**
 * 分页
 * @param <T>
 * @param <K>
 */
public interface Paginator<T, K> {

    /**
     * 不包含总数的分页
     * @param currentPage 当前页
     * @param perPage     每页数量
     * @return 分页信息对象
     * @throws SQLRuntimeException sql错误
     */
    Paginate<T> simplePaginate(int currentPage, int perPage) throws SQLRuntimeException;

    /**
     * 包含总数的分页
     * @param currentPage 当前页
     * @param perPage     每页数量
     * @return 分页信息对象
     * @throws SQLRuntimeException sql错误
     */
    Paginate<T> paginate(int currentPage, int perPage) throws SQLRuntimeException;

}
