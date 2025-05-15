package gaarason.database.contract.record;

import gaarason.database.contract.eloquent.Model;

public interface Support<T, K> {

    /**
     * 数据模型
     * @return 数据模型
     */
    Model<?, T, K> getModel();
}
