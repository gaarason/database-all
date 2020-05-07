package gaarason.database.eloquent;

import gaarason.database.core.lang.Nullable;
import lombok.Data;

import java.util.List;

@Data
public class Paginate<T> {

    private int currentPage;

    private int perPage;

    @Nullable
    private Integer lastPage;

    @Nullable
    private Integer from;

    @Nullable
    private Integer to;

    @Nullable
    private Integer total;

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
    private Integer theFrom(List<T> itemList, int currentPage, int perPage) {
        if (itemList.isEmpty()) {
            return null;
        }
        return (currentPage - 1) * perPage + 1;
    }

    @Nullable
    private Integer theTo(List<T> itemList, int currentPage, int perPage) {
        if (itemList.isEmpty()) {
            return null;
        }
        return (currentPage - 1) * perPage + itemList.size();
    }
}
