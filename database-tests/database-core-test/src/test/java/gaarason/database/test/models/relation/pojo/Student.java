package gaarason.database.test.models.relation.pojo;

import gaarason.database.annotation.*;
import gaarason.database.annotation.base.Relation;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.support.FieldStrategy;
import gaarason.database.eloquent.relation.HasOneOrManyQueryRelation;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.test.models.relation.model.RelationshipStudentTeacherModel;
import gaarason.database.test.models.relation.pojo.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Table(name = "student")
public class Student extends BaseEntity implements Serializable {

    final public static String NAME = "name";
    final public static String AGE = "age";
    final public static String SEX = "sex";
    final public static String TEACHER_ID = "teacher_id";
    final public static String IS_DELETED = "is_deleted";
    final public static String CREATED_AT = "created_at";
    final public static String UPDATED_AT = "updated_at";

    @Column(name = "name", length = 20L, comment = "姓名")
    private String name;

    @Column(name = "age", unsigned = true, comment = "年龄")
    private Integer age;

    @Column(name = "sex", unsigned = true, comment = "性别1男2女")
    private Integer sex;

    @Column(name = "teacher_id", unsigned = true, comment = "教师id")
    private Long teacherId;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "created_at", insertStrategy = FieldStrategy.Never.class, updateStrategy = FieldStrategy.Never.class, comment = "新增时间")
    private Date createdAt;

    @Column(name = "updated_at", insertStrategy = FieldStrategy.Never.class, updateStrategy = FieldStrategy.Never.class, comment = "更新时间")
    private Date updatedAt;

    @BelongsTo(localModelForeignKey = "teacher_id", parentModelLocalKey = "id")
    private Teacher teacher;

    @HasOneOrMany(sonModelForeignKey = "student_id")
    private List<RelationshipStudentTeacher> relationshipStudentTeachers;


    @BelongsToMany(relationModel = RelationshipStudentTeacherModel.class,
        foreignKeyForLocalModel = "student_id", foreignKeyForTargetModel = "teacher_id", localModelLocalKey = "id",
        targetModelLocalKey = "id")
    private List<Teacher> teachersBelongsToMany;

    @HasOne(sonModelForeignKey = "student_id")
    private RelationshipStudentTeacher relationshipStudentTeacher;

    // ------------------------------------ --------------------------------------------//


    @Documented
    @Inherited
    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @Relation(HasOneQueryRelation.class)
    public @interface HasOne {
        /**
         * `子表`中的`关联本表的外键`
         * @return `子表`中的`关联本表的外键`
         */
        String sonModelForeignKey();

        /**
         * `本表`中的`关联键`, 默认值为`本表`的主键(`@Primary()`修饰的键)
         * @return `本表`中的`关联键`
         */
        String localModelLocalKey() default "";

    }


    /**
     * @see HasOneOrManyQueryRelation
     */
    public static class HasOneQueryRelation extends HasOneOrManyQueryRelation {

        public HasOneQueryRelation(Field field, ModelShadowProvider modelShadowProvider, Model<?, ?> model) {
            super(field, modelShadowProvider, model);
        }

        @Override
        protected HasOneOrManyTemplate initTemplate(Field field) {
            HasOne hasOne = field.getAnnotation(HasOne.class);
            Model<?, ?> sonModel = getModelInstance(field);
            String sonModelForeignKey = hasOne.sonModelForeignKey();
            String localModelLocalKey = "".equals(hasOne.localModelLocalKey()) ? getPrimaryKeyColumnName(sonModel) :
                hasOne.localModelLocalKey();
            return new HasOneOrManyTemplate(sonModel, sonModelForeignKey, localModelLocalKey, "", "");
        }

    }
}
