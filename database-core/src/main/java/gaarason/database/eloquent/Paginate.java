package gaarason.database.eloquent;

import gaarason.database.core.lang.Nullable;
import lombok.Data;

import java.util.List;

/**
 * 分页对象
 * @param <T> 分页中的具体数据类型
 * @author xt
 */
@Data
public class Paginate<T> {

    /**
     * 当前页的页码
     */
    protected int currentPage;

    /**
     * 每页数量
     */
    protected int perPage;

    /**
     * 最后页的页码
     */
    @Nullable
    protected Integer lastPage;

    /**
     * 当前页的第一条数据是总数据量中的第几条
     */
    @Nullable
    protected Integer from;

    /**
     * 当前页的最后条数据是总数据量中的第几条
     */
    @Nullable
    protected Integer to;

    /**
     * 总的数据条数
     */
    @Nullable
    protected Integer total;

    /**
     * 当前页的具体数据
     */
    protected List<T> itemList;

    /**
     * 构建分页
     * @param itemList    当前页的具体数据
     * @param currentPage 当前页的页码
     * @param perPage     每页数量
     */
    public Paginate(List<T> itemList, int currentPage, int perPage) {
        this.currentPage = currentPage;
        this.perPage = perPage;
        this.itemList = itemList;
        from = theFrom(itemList, currentPage, perPage);
        to = theTo(itemList, currentPage, perPage);
    }

    /**
     * 构建分页
     * @param itemList    当前页的具体数据
     * @param currentPage 当前页的页码
     * @param perPage     每页数量
     * @param total       数据总量
     */
    public Paginate(List<T> itemList, int currentPage, int perPage, int total) {
        lastPage = Math.max((int) Math.ceil((float) total / perPage), 1);
        this.currentPage = currentPage;
        this.perPage = perPage;
        this.total = total;
        this.itemList = itemList;
        from = theFrom(itemList, currentPage, perPage);
        to = theTo(itemList, currentPage, perPage);
    }

    /**
     * 计算当前页的第一条数据是总数据量中的第几条
     * @param itemList    当前页的具体数据
     * @param currentPage 当前页的页码
     * @param perPage     每页数量
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
     * @param itemList    当前页的具体数据
     * @param currentPage 当前页的页码
     * @param perPage     每页数量
     * @return 当前页的最后条数据是总数据量中的第几条
     */
    @Nullable
    protected Integer theTo(List<T> itemList, int currentPage, int perPage) {
        if (itemList.isEmpty()) {
            return null;
        }
        return (currentPage - 1) * perPage + itemList.size();
    }
}
