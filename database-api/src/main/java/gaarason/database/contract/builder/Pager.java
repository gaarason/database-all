package gaarason.database.contract.builder;

import gaarason.database.appointment.Paginate;
import gaarason.database.exception.SQLRuntimeException;

import java.util.Map;

/**
 * 分页
 * @param <T>
 * @author xt
 */
public interface Pager<T, K> extends Support<T, K> {

    /**
     * 不包含总数的分页
     * @param currentPage 当前页
     * @param perPage 每页数量
     * @return 分页信息对象
     * @throws SQLRuntimeException sql错误
     */
    Paginate<T> simplePaginate(int currentPage, int perPage) throws SQLRuntimeException;

    /**
     * 不包含总数的分页
     * @param currentPage 当前页
     * @param perPage 每页数量
     * @return 分页信息对象
     * @throws SQLRuntimeException sql错误
     */
    default Paginate<T> simplePaginate(Object currentPage, Object perPage) throws SQLRuntimeException {
        return simplePaginate(conversionToInt(currentPage), conversionToInt(perPage));
    }

    /**
     * 不包含总数的分页
     * @param currentPage 当前页
     * @param perPage 每页数量
     * @return 分页信息对象
     * @throws SQLRuntimeException sql错误
     */
    Paginate<Map<String, Object>> simplePaginateMapStyle(int currentPage, int perPage) throws SQLRuntimeException;

    /**
     * 不包含总数的分页
     * @param currentPage 当前页
     * @param perPage 每页数量
     * @return 分页信息对象
     * @throws SQLRuntimeException sql错误
     */
    default Paginate<Map<String, Object>> simplePaginateMapStyle(Object currentPage, Object perPage)
        throws SQLRuntimeException {
        return simplePaginateMapStyle(conversionToInt(currentPage), conversionToInt(perPage));
    }

    /**
     * 包含总数的分页
     * @param currentPage 当前页
     * @param perPage 每页数量
     * @return 分页信息对象
     * @throws SQLRuntimeException sql错误
     */
    Paginate<T> paginate(int currentPage, int perPage) throws SQLRuntimeException;

    /**
     * 包含总数的分页
     * @param currentPage 当前页
     * @param perPage 每页数量
     * @return 分页信息对象
     * @throws SQLRuntimeException sql错误
     */
    default Paginate<T> paginate(Object currentPage, Object perPage) throws SQLRuntimeException {
        return paginate(conversionToInt(currentPage), conversionToInt(perPage));
    }

    /**
     * 包含总数的分页
     * @param currentPage 当前页
     * @param perPage 每页数量
     * @return 分页信息对象
     * @throws SQLRuntimeException sql错误
     */
    Paginate<Map<String, Object>> paginateMapStyle(int currentPage, int perPage) throws SQLRuntimeException;

    /**
     * 包含总数的分页
     * @param currentPage 当前页
     * @param perPage 每页数量
     * @return 分页信息对象
     * @throws SQLRuntimeException sql错误
     */
    default Paginate<Map<String, Object>> paginateMapStyle(Object currentPage, Object perPage)
        throws SQLRuntimeException {
        return paginateMapStyle(conversionToInt(currentPage), conversionToInt(perPage));
    }

}
