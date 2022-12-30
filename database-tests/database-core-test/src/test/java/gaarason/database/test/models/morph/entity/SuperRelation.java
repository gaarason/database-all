package gaarason.database.test.models.morph.entity;

import gaarason.database.annotation.Column;
import gaarason.database.annotation.Table;
import gaarason.database.contract.support.FieldConversion;
import gaarason.database.contract.support.FieldFill;
import gaarason.database.contract.support.FieldStrategy;
import gaarason.database.test.models.morph.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Table(name = "super_relation")
public class SuperRelation extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** auto generator start **/


    @Column(name = "relation_one_type", length = 200L)
    private String relationOneType;

    @Column(name = "relation_one_value", unsigned = true)
    private Long relationOneValue;

    @Column(name = "relation_two_type", length = 200L)
    private String relationTwoType;

    @Column(name = "relation_two_value", unsigned = true)
    private Long relationTwoValue;

    @Column(name = "created_at", selectable = true, fill = FieldFill.NotFill.class, strategy = FieldStrategy.Default.class, insertStrategy = FieldStrategy.Never.class, updateStrategy = FieldStrategy.Never.class, conditionStrategy = FieldStrategy.Default.class, conversion = FieldConversion.Default.class, comment = "新增时间")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", selectable = true, fill = FieldFill.NotFill.class, strategy = FieldStrategy.Default.class, insertStrategy = FieldStrategy.Never.class, updateStrategy = FieldStrategy.Never.class, conditionStrategy = FieldStrategy.Default.class, conversion = FieldConversion.Default.class, comment = "更新时间")
    private LocalDateTime updatedAt;


    /** auto generator end **/

    public static class Model extends BaseEntity.BaseModel<SuperRelation, Long> {

    }

}