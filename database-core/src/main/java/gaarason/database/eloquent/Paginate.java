package gaarason.database.eloquent;

import gaarason.database.core.lang.Nullable;
import lombok.Data;

import java.util.List;

@Data
public class Paginate<T> {

    protected int     currentPage;

    protected int     perPage;

    @Nullable
    protected Integer lastPage;

    @Nullable
    protected Integer from;

    @Nullable
    protected Integer to;

    @Nullable
    protected Integer total;

    List<T> itemList;

    public Paginate(List<T> itemList, int currentPage, int perPage) {
        this.currentPage = currentPage;
        this.perPage = perPage;
        this.itemList = itemList;
        from = theFrom(itemList, currentPage, perPage);
        to = theTo(itemList, currentPage, perPage);
    }

    public Paginate(List<T> itemList, int currentPage, int perPage, int total) {
        lastPage = Math.max((int) Math.ceil((float) total / perPage), 1);
        this.currentPage = currentPage;
        this.perPage = perPage;
        this.total = total;
        this.itemList = itemList;
        from = theFrom(itemList, currentPage, perPage);
        to = theTo(itemList, currentPage, perPage);
    }

    @Nullable
    protected Integer theFrom(List<T> itemList, int currentPage, int perPage) {
        if (itemList.isEmpty()) {
            return null;
        }
        return (currentPage - 1) * perPage + 1;
    }

    @Nullable
    protected Integer theTo(List<T> itemList, int currentPage, int perPage) {
        if (itemList.isEmpty()) {
            return null;
        }
        return (currentPage - 1) * perPage + itemList.size();
    }
}
