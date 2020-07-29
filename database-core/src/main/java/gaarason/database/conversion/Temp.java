package gaarason.database.conversion;

import com.fasterxml.jackson.databind.util.BeanUtil;
import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.contracts.function.RelationshipRecordWith;
import gaarason.database.eloquent.RecordList;
import gaarason.database.eloquent.relations.HasOneQuery;
import gaarason.database.support.Column;
import lombok.Data;

import java.beans.Beans;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 暂存

/**
 *
 * @param <T> 属性的类型
 * @param <K> 属性的键名
 */
@Data
public class Temp<T, K> {

    /**
     * 是否的list结果
     */
    private boolean list;

    /**
     * 当前的 对象类
     */
    private Class<T> entity;

    /**
     * 关系类型 hasOne, belongsTo, ==
     */
    private String relation;

    /**
     * 字段的反射对象
     */
    private Field field;

    /**
     * 全量父级数据
     */
    private List<Map<String, Column>> originalMetadataMapList = new ArrayList<>();

    private GenerateSqlPart generateSqlPart;

    private RelationshipRecordWith relationshipRecordWith;

    /**
     * 主键 ??
     * 对象
     * 主键与对象关系, 为后续filter做准备
     */
    private Map<Object, Object> idEntityObj = new HashMap<>();


    public void run(){
        if("hasOne".equals(relation)){


            RecordList<?, ?> records = HasOneQuery.dealBatch(field, originalMetadataMapList, generateSqlPart,
                relationshipRecordWith);


            for (Map.Entry<Object, Object> objectObjectEntry : idEntityObj.entrySet()) {

                Object value = objectObjectEntry.getValue();
                Object key   = objectObjectEntry.getKey();

                Object o = HasOneQuery.filterBatch222222222222(field, key.toString(), records);

                // BeanUtils.copyProperties(request.getWorkFlow().getInitiator(), initiator);



            }
//            HasOneQuery.filterBatch222222222222(field, , records);


            System.out.println(" run ok ");
        }else{
            System.out.println("空执行");
        }
    }

}
