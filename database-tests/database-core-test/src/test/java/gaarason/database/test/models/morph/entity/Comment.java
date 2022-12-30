package gaarason.database.test.models.morph.entity;

import gaarason.database.annotation.*;
import gaarason.database.contract.support.FieldConversion;
import gaarason.database.contract.support.FieldFill;
import gaarason.database.contract.support.FieldStrategy;
import gaarason.database.test.models.morph.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

@Data
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Table(name = "comment")
public class Comment extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** auto generator start **/


    @Column(name = "p_type", length = 200L, comment = "回复的类型")
    private String pType;

    @Column(name = "p_id", unsigned = true, comment = "回复的类型的id")
    private Long pId;

    @Column(name = "content", length = 200L, comment = "内容")
    private String content;

    @Column(name = "created_at", selectable = true, fill = FieldFill.NotFill.class, strategy = FieldStrategy.Default.class, insertStrategy = FieldStrategy.Never.class, updateStrategy = FieldStrategy.Never.class, conditionStrategy = FieldStrategy.Default.class, conversion = FieldConversion.Default.class, comment = "新增时间")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", selectable = true, fill = FieldFill.NotFill.class, strategy = FieldStrategy.Default.class, insertStrategy = FieldStrategy.Never.class, updateStrategy = FieldStrategy.Never.class, conditionStrategy = FieldStrategy.Default.class, conversion = FieldConversion.Default.class, comment = "更新时间")
    private LocalDateTime updatedAt;

    /** auto generator end **/

    // 省略了`localModelMorphValue`, 表示当 p_type 的值为 Post的表名时, 关系成立
    @BelongsTo(localModelForeignKey = "p_id", localModelMorphKey = "p_type")
    private Post post;

    // 省略了`localModelMorphValue`, 表示当 p_type 的值为 Comment的表名时, 关系成立
    @BelongsTo(localModelForeignKey = "p_id", localModelMorphKey = "p_type")
    private Comment pcomment;

    @HasOneOrMany(sonModelForeignKey = "p_id", sonModelMorphKey = "p_type")
    private Comment comment;

    // 省略了`sonModelMorphValue`, 表示当 p_type 的值为 Comment的表名时, 关系成立
    @HasOneOrMany(sonModelForeignKey = "p_id", sonModelMorphKey = "p_type")
    private List<Comment> comments;

    @BelongsToMany(relationModel = SuperRelation.Model.class, foreignKeyForLocalModel = "relation_one_value", foreignKeyForTargetModel = "relation_two_value",
        morphKeyForLocalModel = "relation_one_type", morphKeyForTargetModel = "relation_two_type")
    private List<Image> images;

    public static class Model extends BaseEntity.BaseModel<Comment, Long> {

    }

}