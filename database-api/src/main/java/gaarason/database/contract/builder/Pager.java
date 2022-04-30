package gaarason.database.contract.builder;

import gaarason.database.appointment.Paginate;
import gaarason.database.exception.SQLRuntimeException;

import java.io.Serializable;
import java.util.Map;

/**
 * 分页
 * @param <T>
 * @author xt
 */
public interface Pager<T extends Serializable> {

    /**
     * 不包含总数的分页
     * @param currentPage 当前页
     * @param perPage     每页数量
     * @return 分页信息对象
     * @throws SQLRuntimeException sql错误
     */
    Paginate<T> simplePaginate(int currentPage, int perPage) throws SQLRuntimeException;

    /**
     * 不包含总数的分页
     * @param currentPage 当前页
     * @param perPage     每页数量
     * @return 分页信息对象
     * @throws SQLRuntimeException sql错误
     */
    Paginate<Map<String, Object>> simplePaginateMapStyle(int currentPage, int perPage) throws SQLRuntimeException;

    /**
     * 包含总数的分页
     * @param currentPage 当前页
     * @param perPage     每页数量
     * @return 分页信息对象
     * @throws SQLRuntimeException sql错误
     */
    Paginate<T> paginate(int currentPage, int perPage) throws SQLRuntimeException;

    /**
     * 包含总数的分页
     * @param currentPage 当前页
     * @param perPage     每页数量
     * @return 分页信息对象
     * @throws SQLRuntimeException sql错误
     */
    Paginate<Map<String, Object>> paginateMapStyle(int currentPage, int perPage) throws SQLRuntimeException;

}