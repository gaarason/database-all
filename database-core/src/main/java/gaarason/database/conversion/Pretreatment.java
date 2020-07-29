package gaarason.database.conversion;

import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.contracts.function.RelationshipRecordWith;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 预处理
 * @param <T> 对象类型
 * @param <K> 主键类型
 */
@Data
public class Pretreatment<T, K> {

    /**
     * 当前sql预处理
     */
    private GenerateSqlPart generateSqlPart;

    /**
     * 下级sql预处理
     */
    private RelationshipRecordWith relationshipRecordWith;


    /**
     * HasOne, HasMany, BelongsToMany, BelongsTo
     */
    private relationType type;

    /**
     *
     */
    private Field field;

    /**
     * 主键名
     */
    private String primaryKeyName;

    /**
     * 主键值 => 暂存对象
     * primaryKeyValue => ObjSet
     */
    private Map<K, T> primaryKeyObjectMap = new HashMap<>();


    enum relationType {
        BELONGS_TO_MANY, BELONGS_TO, HAS_MANY, HAS_ONE
    }
}
