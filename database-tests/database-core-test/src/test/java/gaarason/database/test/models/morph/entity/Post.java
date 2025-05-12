package gaarason.database.test.models.morph.entity;

import gaarason.database.annotation.BelongsToMany;
import gaarason.database.annotation.Column;
import gaarason.database.annotation.HasOneOrMany;
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
import java.util.List;


@Data
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Table(name = "post")
public class Post extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** auto generator start **/


    @Column(name = "title", length = 20L, comment = "标题")
    private String title;

    @Column(name = "content", length = 200L, comment = "内容")
    private String content;

    @Column(name = "created_at", selectable = true, fill = FieldFill.NotFill.class, strategy = FieldStrategy.Default.class, insertStrategy = FieldStrategy.Never.class, updateStrategy = FieldStrategy.Never.class, conditionStrategy = FieldStrategy.Default.class, conversion = FieldConversion.SimpleValue.class, comment = "新增时间")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", selectable = true, fill = FieldFill.NotFill.class, strategy = FieldStrategy.Default.class, insertStrategy = FieldStrategy.Never.class, updateStrategy = FieldStrategy.Never.class, conditionStrategy = FieldStrategy.Default.class, conversion = FieldConversion.SimpleValue.class, comment = "更新时间")
    private LocalDateTime updatedAt;

    /** auto generator end **/

    // 省略了`sonModelMorphValue`, 表示当 p_type 的值为 Post的表名时, 关系成立
    @HasOneOrMany(sonModelForeignKey = "p_id", sonModelMorphKey = "p_type")
    private Comment comment;

    @HasOneOrMany(sonModelForeignKey = "p_id", sonModelMorphKey = "p_type")
    private List<Comment> comments;

    @BelongsToMany(relationModel = SuperRelation.Model.class, foreignKeyForLocalModel = "relation_one_value", foreignKeyForTargetModel = "relation_two_value")
    private List<Image> images;

    // 省略了`morphValueForLocalModel`, 表示当 relation_one_value 的值为 Post的表名时, 和本表(post)关系成立
    // 省略了`morphValueForTargetModel`, 表示当 relation_two_value 的值为 Image的表名时, 和目标表(image)关系成立
    // 当上述均成立时, 关系成立
    @BelongsToMany(relationModel = SuperRelation.Model.class, foreignKeyForLocalModel = "relation_one_value", foreignKeyForTargetModel = "relation_two_value",
        morphKeyForLocalModel = "relation_one_type", morphKeyForTargetModel = "relation_two_type")
    private List<Image> imagesWithMorph;

    @BelongsToMany(relationModel = SuperRelation.Model.class, foreignKeyForLocalModel = "relation_one_value", foreignKeyForTargetModel = "relation_two_value",
        morphKeyForLocalModel = "relation_one_type")
    private List<Image> imagesWithLocalMorph;

    @BelongsToMany(relationModel = SuperRelation.Model.class, foreignKeyForLocalModel = "relation_one_value", foreignKeyForTargetModel = "relation_two_value",
        morphKeyForTargetModel = "relation_two_type")
    private List<Image> imagesWithTargetMorph;;

    public static class Model extends BaseEntity.BaseModel<Post, Long> {

    }

}