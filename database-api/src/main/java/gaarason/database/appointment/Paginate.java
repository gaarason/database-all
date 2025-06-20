package gaarason.database.appointment;

import gaarason.database.lang.Nullable;

import java.io.Serializable;
import java.util.List;

/**
 * 分页对象
 * @param <T> 分页中的具体数据类型
 * @author xt
 */
public class Paginate<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页的页码
     */
    protected final int currentPage;

    /**
     * 每页数量
     */
    protected final int perPage;

    /**
     * 最后页的页码
     */
    @Nullable
    protected Integer lastPage;

    /**
     * 当前页的第一条数据是总数据量中的第几条
     */
    @Nullable
    protected final Integer from;

    /**
     * 当前页的最后条数据是总数据量中的第几条
     */
    @Nullable
    protected final Integer to;

    /**
     * 总的数据条数
     */
    @Nullable
    protected Long total;

    /**
     * 当前页的具体数据
     */
    protected final List<T> itemList;

    /**
     * 构建分页
     * @param itemList 当前页的具体数据
     * @param currentPage 当前页的页码
     * @param perPage 每页数量
     * @param total 数据总量
     */
    public Paginate(List<T> itemList, int currentPage, int perPage, @Nullable Long total) {
        lastPage = null == total ? null : Math.max((int) Math.ceil((float) total / perPage), 1);
        this.currentPage = currentPage;
        this.perPage = perPage;
        this.total = total;
        this.itemList = itemList;
        from = theFrom(itemList, currentPage, perPage);
        to = theTo(itemList, currentPage, perPage);
    }

    /**
     * 计算当前页的第一条数据是总数据量中的第几条
     * @param itemList 当前页的具体数据
     * @param currentPage 当前页的页码
     * @param perPage 每页数量
     * @return 当前页的第一条数据是总数据量中的第几条
     */
    @Nullable
    protected Integer theFrom(List<T> itemList, int currentPage, int perPage) {
        if (itemList.isEmpty()) {
            return null;
        }
        return (currentPage - 1) * perPage + 1;
    }

    /**
     * 计算当前页的最后条数据是总数据量中的第几条
     * @param itemList 当前页的具体数据
     * @param currentPage 当前页的页码
     * @param perPage 每页数量
     * @return 当前页的最后条数据是总数据量中的第几条
     */
    @Nullable
    protected Integer theTo(List<T> itemList, int currentPage, int perPage) {
        if (itemList.isEmpty()) {
            return null;
        }
        return (currentPage - 1) * perPage + itemList.size();
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPerPage() {
        return perPage;
    }

    @Nullable
    public Integer getLastPage() {
        return lastPage;
    }

    @Nullable
    public Integer getFrom() {
        return from;
    }

    @Nullable
    public Integer getTo() {
        return to;
    }

    @Nullable
    public Long getTotal() {
        return total;
    }

    public List<T> getItemList() {
        return itemList;
    }

    @Override
    public String toString() {
        return "Paginate{" +
            "currentPage=" + currentPage +
            ", perPage=" + perPage +
            ", lastPage=" + lastPage +
            ", from=" + from +
            ", to=" + to +
            ", total=" + total +
            ", itemList=" + itemList +
            '}';
    }
}
