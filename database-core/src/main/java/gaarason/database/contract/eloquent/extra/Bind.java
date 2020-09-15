package gaarason.database.contract.eloquent.extra;

import gaarason.database.contract.record.bind.Attach;
import gaarason.database.contract.record.bind.Detach;
import gaarason.database.contract.record.bind.Sync;
import gaarason.database.contract.record.bind.Toggle;

public interface Bind extends Attach, Detach, Sync, Toggle {

}
