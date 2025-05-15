package gaarason.database.contract.model;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.model.base.QueryEvent;
import gaarason.database.contract.model.base.RecordEvent;

public interface Event<B extends Builder<B, T, K>, T, K> extends RecordEvent<T, K>, QueryEvent<B, T, K> {

}
