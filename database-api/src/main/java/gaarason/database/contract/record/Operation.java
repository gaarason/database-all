package gaarason.database.contract.record;

import gaarason.database.appointment.Column;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.exception.EntityAttributeInvalidException;
import gaarason.database.lang.Nullable;

import java.io.Serializable;
import java.util.Map;

/**
 * ORM 操作
 * @author xt
 */
public interface Operation<T extends Serializable, K extends Serializable> {

    /**
     * 新增或者更新
     * 新增情况下: saving -> creating -> created -> saved
     * 更新情况下: saving -> updating -> updated -> saved
     * @return 执行成功
     */
    boolean save();

    /**
     * 删除
     * deleting -> deleted
     * @return 执行成功
     */
    boolean delete();

    /**
     * 恢复(成功恢复后将会刷新record)
     * restoring -> restored
     * @return 执行成功
     */
    boolean restore();

    /**
     * 恢复
     * restoring -> restored
     * @param refresh 是否刷新自身
     * @return 执行成功
     */
    boolean restore(boolean refresh);

    /**
     * 刷新(重新从数据库获取)
     * retrieved
     * @return 执行成功
     */
    Record<T, K> refresh();

    /**
     * 刷新(重新从数据获取)
     * retrieved
     * @param metadataMap 使用的元数据
     * @return 执行成功
     */
    Record<T, K> refresh(Map<String, Column> metadataMap);

    /**
     * 是否有任何属性发生改变
     * @return bool
     */
    boolean isDirty();

    /**
     * 获取所有变更属性组成的map
     * 不会包含关联关系检测
     * @return map<列名, 值>
     */
    Map<String, Object> getDirtyMap();

    /**
     * 指定属性是否有发生改变
     * @param fieldName 实体属性名
     * @return bool
     */
    boolean isDirty(String fieldName);

    /**
     * 是否没有任何属性发生改变
     * @return bool
     */
    boolean isClean();

    /**
     * 指定属性是否没有发生改变
     * @param fieldName 实体属性名
     * @return bool
     */
    boolean isClean(String fieldName);

    /**
     * 返回一个包含模型原生属性的实体
     * @return 实体对象
     */
    T getOriginal();

    /**
     * 返回模型原生属性的值
     * @param fieldName 实体属性名
     * @return 实体对象
     * @throws EntityAttributeInvalidException 无效的属性(实体声明中没有的属性)
     */
    @Nullable
    Object getOriginal(String fieldName) throws EntityAttributeInvalidException;

}
