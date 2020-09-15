package gaarason.database.contract.record.bind;

import gaarason.database.eloquent.Record;

public interface DetachTrait {

    boolean detach(Record<?, ?> targetRecord);

    boolean detach(String id);
}
