package gaarason.database.contract.model;

import gaarason.database.appointment.FinalVariable;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Record;

/**
 * 软删除
 * @param <T> 实体类
 * @param <K> 主键类型
 * @author xt
 */
public interface SoftDelete<T, K> {

    /**
     * 删除(软/硬删除)
     * @param builder 查询构造器
     * @return 删除的行数
     */
    int delete(Builder<?, T, K> builder);

    /**
     * 恢复软删除
     * @param builder 查询构造器
     * @return 删除的行数
     */
    int restore(Builder<?, T, K> builder);

    /**
     * 是否启用软删除
     * @return 是否启用软删除
     */
    default boolean isSoftDeleting() {
        return false;
    }

    /**
     * 软删除标记的列名
     * 在以`AutoRestore`为后缀的方法中, 命中已软删除记录时会从更新数据里剔除该列, 以避免与恢复数据的同列重复赋值
     * @return 软删除标记的列名
     */
    default String softDeletedColumnName() {
        return FinalVariable.DEFAULT_SOFT_DELETED_COLUMN_NAME;
    }

    /**
     * 记录是否处于已软删除状态
     * 默认实现: 读取记录(查询结果)中软删除标记列的值; 若自定义了软删除语义(如以时间戳作为标记), 可重写
     * @param record 记录(其查询结果中需包含软删除标记列)
     * @return 记录是否处于已软删除状态
     */
    default boolean isSoftDeleted(Record<T, K> record) {
        Object value = record.getMetadataMap().get(softDeletedColumnName());
        return Boolean.TRUE.equals(value) || FinalVariable.DEFAULT_SOFT_DELETED_VALUE_YSE.equals(String.valueOf(value));
    }
}
