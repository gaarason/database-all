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
     * 光标位置, 用于查询下一页
     */
    protected final Object index;

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
     * @param index 当前页的页码
     * @param perPage 每页数量
     */
    public CursorPaginate(List<T> itemList, Object index, int perPage, @Nullable Long total) {
        this.itemList = itemList;
        this.index = index;
        this.perPage = perPage;
        this.total = total;
    }

    public Object getIndex() {
        return index;
    }

    public int getPerPage() {
        return perPage;
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
        return "CursorPaginate{" +
                "index=" + index +
                ", perPage=" + perPage +
                ", total=" + total +
                ", itemList=" + itemList +
                '}';
    }
}
