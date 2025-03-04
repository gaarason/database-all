package gaarason.database.appointment;

import gaarason.database.contract.eloquent.RecordList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 关联关系中的的缓存对象
 */
public class RelationCache {

    /**
     * 查询结果集缓存
     * 用于优化 (减少) sql 查询量
     */
    public final Map<String, RecordList<?, ?>> cacheRelationRecordList = new HashMap<>();

    /**
     * 对象缓存
     * 用于优化 查询结果到对象的转化耗时
     */
    public final Map<String, List<?>> cacheRelationObjectList = new HashMap<>();

    /**
     * 当前执行层级
     * 以确保 cacheRelationObjectList 命中的准确性
     */
    public final AtomicInteger level = new AtomicInteger();

}
