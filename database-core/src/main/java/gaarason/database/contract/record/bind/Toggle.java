package gaarason.database.contract.record.bind;

import gaarason.database.eloquent.RecordList;

import java.util.Collection;
import java.util.Map;

public interface Toggle {



    boolean toggle(RecordList<?, ?> targetRecords);

    boolean toggle(Collection<String> ids);

    boolean toggle(RecordList<?, ?> targetRecords, Map<String, String> stringStringMap);

    boolean toggle(Collection<String> ids, Map<String, String> stringStringMap);
}
