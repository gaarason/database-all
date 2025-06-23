package gaarason.database.appointment;

import gaarason.database.lang.Nullable;

import java.io.Serializable;
import java.util.List;

/**
 * 光标分页对象
 * @param <T> 分页中的具体数据类型
 * @author xt
 */
public class CursorPaginate<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页的具体数据
     */
    protected final List<T> itemList;

    /**
     * 光标位置, 用于查询上一页
     */
    @Nullable
    protected final Object previousIndex;

    /**
     * 光标位置, 用于查询下一页
     */
    @Nullable
    protected final Object nextIndex;

    /**
     * 每页数量
     */
    protected final int perPage;

    /**
     * 总的数据条数
     */
    @Nullable
    protected Long total;

    /**
     * 构建分页
     * @param itemList 当前页的具体数据
     * @param previousIndex 光标位置, 用于查询上一页
     * @param nextIndex 光标位置, 用于查询下一页
     * @param perPage 每页数量
     */
    public CursorPaginate(List<T> itemList, @Nullable Object previousIndex, @Nullable Object nextIndex,
            int perPage, @Nullable Long total) {
        this.itemList = itemList;
        this.previousIndex = previousIndex;
        this.nextIndex = nextIndex;
        this.perPage = perPage;
        this.total = total;
    }

    public List<T> getItemList() {
        return itemList;
    }

    @Nullable
    public Object getPreviousIndex() {
        return previousIndex;
    }

    @Nullable
    public Object getNextIndex() {
        return nextIndex;
    }

    public int getPerPage() {
        return perPage;
    }

    @Nullable
    public Long getTotal() {
        return total;
    }

    @Override
    public String toString() {
        return "CursorPaginate{" +
                "itemList=" + itemList +
                ", previousIndex=" + previousIndex +
                ", nextIndex=" + nextIndex +
                ", perPage=" + perPage +
                ", total=" + total +
                '}';
    }
}
