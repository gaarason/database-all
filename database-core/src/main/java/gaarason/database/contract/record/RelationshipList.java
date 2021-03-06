package gaarason.database.contract.record;

import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.contract.function.RelationshipRecordWithFunctionalInterface;

/**
 * 关联关系
 * @param <T> 实体类
 * @param <K> 主键类型
 */
public interface RelationshipList<T, K> {


    /**
     * 渴求式关联
     * @param column 所关联的Model(当前模块的属性名)
     * @return 关联的Model的查询构造器
     */
    RecordList<T, K> with(String column);

    /**
     * 渴求式关联
     * @param column         所关联的Model(当前模块的属性名)
     * @param builderClosure 所关联的Model的查询构造器约束
     * @return 关联的Model的查询构造器
     */
    RecordList<T, K> with(String column, GenerateSqlPartFunctionalInterface builderClosure);

    /**
     * 渴求式关联
     * @param column         所关联的Model(当前模块的属性名)
     * @param builderClosure 所关联的Model的查询构造器约束
     * @param recordClosure  所关联的Model的再一级关联
     * @return 关联的Model的查询构造器
     */
    RecordList<T, K> with(String column, GenerateSqlPartFunctionalInterface builderClosure,
                          RelationshipRecordWithFunctionalInterface recordClosure);
}
