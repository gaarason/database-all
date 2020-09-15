package gaarason.database.contract.eloquent;

import gaarason.database.contract.record.Friendly;
import gaarason.database.contract.record.Operation;
import gaarason.database.contract.record.Relationship;

import java.io.Serializable;

public interface Record<T, K> extends Friendly<T, K>, Operation<T, K>, Relationship<T, K>, Serializable {

}
