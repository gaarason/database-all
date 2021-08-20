package gaarason.database.contract.record.bind;

import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;

import java.util.Collection;

/**
 * 解除关系
 * @author xt
 */
public interface Detach {

    /**
     * 全部单个关系
     * @return 受影响的行数
     */
    int detach();

    /**
     * 解除单个关系
     * @param targetRecord 目标record
     * @return 受影响的行数
     */
    int detach(Record<?, ?> targetRecord);

    /**
     * 解除多个关系
     * @param targetRecords 目标records
     * @return 受影响的行数
     */
    int detach(RecordList<?, ?> targetRecords);

    /**
     * 解除单个关系
     * @param id 目标record的主键
     * @return 受影响的行数
     */
    int detach(String id);

    /**
     * 解除多个关系
     * @param ids 目标records的主键集合
     * @return 受影响的行数
     */
    int detach(Collection<String> ids);
}
