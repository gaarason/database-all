package gaarason.database.support;

import gaarason.database.contracts.eloquent.relations.SubQuery;
import gaarason.database.contracts.function.GenerateRecordList;
import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.contracts.function.RelationshipRecordWith;
import gaarason.database.eloquent.Model;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.RecordList;
import gaarason.database.eloquent.annotations.BelongsTo;
import gaarason.database.eloquent.annotations.BelongsToMany;
import gaarason.database.eloquent.annotations.HasOneOrMany;
import gaarason.database.eloquent.relations.BelongsToManyQuery;
import gaarason.database.eloquent.relations.BelongsToQuery;
import gaarason.database.eloquent.relations.HasOneOrManyQuery;
import gaarason.database.exception.EntityNewInstanceException;
import gaarason.database.exception.RelationAnnotationNotSupportedException;
import gaarason.database.utils.EntityUtil;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.*;

public class RelationSaveSupport<T, K> {
//
//    /**
//     * 需要处理的实体对象集合
//     */
//    protected final Collection<T> entities;

    /**
     * 当前结果集
     */
    protected final RecordList<T, K> records;

    /**
     * 是否启用关联关系
     * 在启用时, 需要手动指定(with)才会生效
     * 在不启用时, 即使手动指定(with)也不会生效
     */
    protected final boolean attachedRelationship;


    protected Map<String , Object> map = new LinkedHashMap<>();


    /**
     * 基本对象转化
     * @param record               结果集
     * @param attachedRelationship 是否启用关联关系
     */
    public RelationSaveSupport(Record<T, K> record, boolean attachedRelationship) {
        List<Record<T, K>> records = new ArrayList<>();
        records.add(record);
        this.attachedRelationship = attachedRelationship;
        this.records = RecordFactory.newRecordList(records);
    }

    public RelationSaveSupport(List<Record<T, K>> records, boolean attachedRelationship) {
        this.attachedRelationship = attachedRelationship;
        this.records = RecordFactory.newRecordList(records);
    }

    public RelationSaveSupport(RecordList<T, K> records, boolean attachedRelationship) {
        this.attachedRelationship = attachedRelationship;
        this.records = records;
    }


    public boolean save(){
        return saveMany().get(0);
    }
    public List<Boolean> saveMany(){

        Info info = new Info();

        return new ArrayList<>();
    }

    public boolean insert() {
        // 检测关联关系是否存在


        // 1. 遍历检索



        // 暂不支持belongTo

        // 存在则开始事物, 搞起


return true;
        // return toObjectList().get(0);
    }


    protected void save并更新entity(){



    }


    static class Info<T> {
        protected SubQuery subQuery;
        protected Map<SubQueryValue, List<T>> tMap = new HashMap<>();
    }


    static class SubQueryValue{

    }
}
