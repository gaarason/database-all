package gaarason.database.contract.record;

import gaarason.database.core.lang.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xt
 * @since 2021/11/8 7:04 下午
 */
public interface CollectionOperation<E> extends List<E> {

    @Nullable
    Object getValueByFieldName(E element, String fieldName);

    default Map<Object, E> keyBy(String fieldName) {
        Map<Object, E> theMap = new HashMap<>(16);
        forEach(element -> {
            theMap.put(getValueByFieldName(element, fieldName), element);
        });
        return theMap;
    }
}
