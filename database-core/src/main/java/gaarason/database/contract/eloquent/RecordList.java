package gaarason.database.contract.eloquent;

import gaarason.database.contract.record.FriendlyList;
import gaarason.database.contract.record.RelationshipList;
import gaarason.database.eloquent.Record;

import java.io.Serializable;
import java.util.List;
import java.util.RandomAccess;

public interface RecordList<T, K> extends FriendlyList<T, K>,
    RelationshipList<T, K>, List<Record<T, K>>, RandomAccess, Cloneable, Serializable {
}
