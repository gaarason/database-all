package gaarason.database.contract.record.bind;

import gaarason.database.eloquent.RecordList;

import java.util.Collection;
import java.util.Map;

public interface SyncTrait {



    boolean sync(RecordList<?, ?> targetRecords);

    boolean sync(Collection<String> ids);

    boolean sync(RecordList<?, ?> targetRecords, Map<String, String> stringStringMap);

    boolean sync(Collection<String> ids, Map<String, String> stringStringMap);
}
