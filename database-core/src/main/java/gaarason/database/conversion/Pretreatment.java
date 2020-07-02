package gaarason.database.conversion;

import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.contracts.function.RelationshipRecordWith;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Field;

public class Pretreatment {



    private GenerateSqlPart generateSqlPart;

    private RelationshipRecordWith relationshipRecordWith;


    @Data
    @AllArgsConstructor
    static class dd {
        // HasOne, HasMany, BelongsToMany, BelongsTo
        String type;

        Field field;

        // 主键名
        String primaryKeyName;

        // 主键值
        String primaryKeyValue;
    }

    enum relationType{
        BELONGS_TO_MANY,BELONGS_TO,HAS_MANY,HAS_ONE
    }
}
