package gaarason.database.test.models.relation.pojo;

import gaarason.database.annotation.BelongsToMany;
import gaarason.database.annotation.Column;
import gaarason.database.annotation.HasOneOrMany;
import gaarason.database.annotation.Table;
import gaarason.database.contract.support.FieldStrategy;
import gaarason.database.test.models.relation.model.RelationshipStudentTeacherModel;
import gaarason.database.test.models.relation.pojo.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.*;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Table(name = "teacher")
public class Teacher extends BaseEntity implements Serializable {

    final public static String NAME = "name";
    final public static String AGE = "age";
    final public static String SEX = "sex";
    final public static String SUBJECT = "subject";
    final public static String CREATED_AT = "created_at";
    final public static String UPDATED_AT = "updated_at";

    @Column(name = "name", length = 20L, comment = "姓名")
    private String name;

    @Column(name = "age", unsigned = true, comment = "年龄")
    private Integer age;

    @Column(name = "sex", unsigned = true, comment = "性别1男2女")
    private Integer sex;

    @Column(name = "subject", length = 20L, comment = "科目")
    private String subject;

    @Column(name = "created_at", insertStrategy = FieldStrategy.Never.class, updateStrategy = FieldStrategy.Never.class, comment = "新增时间")
    private Date createdAt;

    @Column(name = "updated_at", insertStrategy = FieldStrategy.Never.class, updateStrategy = FieldStrategy.Never.class, comment = "更新时间")
    private Date updatedAt;

    @HasOneOrMany(sonModelForeignKey = "teacher_id")
    private List<Student> students;

    @HasOneOrMany(sonModelForeignKey = "teacher_id")
    private Student[] studentArray;

    @HasOneOrMany(sonModelForeignKey = "teacher_id")
    private ArrayList<Student> studentArrayList;

    @HasOneOrMany(sonModelForeignKey = "teacher_id")
    private LinkedList<Student> studentLinkedList;

    @HasOneOrMany(sonModelForeignKey = "teacher_id")
    private Set<Student> studentSet;

    @HasOneOrMany(sonModelForeignKey = "teacher_id")
    private LinkedHashSet<Student> studentLinkedHashSet;

    @HasOneOrMany(sonModelForeignKey = "teacher_id", localModelLocalKey = "id")
    private Student student;

    @HasOneOrMany(sonModelForeignKey = "teacher_id")
    private List<RelationshipStudentTeacher> relationshipStudentTeachers;

    @BelongsToMany(relationModel = RelationshipStudentTeacherModel.class, foreignKeyForLocalModel = "teacher_id", foreignKeyForTargetModel = "student_id", localModelLocalKey = "id", targetModelLocalKey = "id")
    private List<Student> studentsBelongsToMany;

    @BelongsToMany(relationModel = RelationshipStudentTeacherModel.class, foreignKeyForLocalModel = "teacher_id", foreignKeyForTargetModel = "student_id", localModelLocalKey = "id", targetModelLocalKey = "id")
    private Student[] studentsBelongsToManyArray;

    @BelongsToMany(relationModel = RelationshipStudentTeacherModel.class, foreignKeyForLocalModel = "teacher_id", foreignKeyForTargetModel = "student_id", localModelLocalKey = "id", targetModelLocalKey = "id")
    private ArrayList<Student> studentsBelongsToManyArrayList;

    @BelongsToMany(relationModel = RelationshipStudentTeacherModel.class, foreignKeyForLocalModel = "teacher_id", foreignKeyForTargetModel = "student_id", localModelLocalKey = "id", targetModelLocalKey = "id")
    private Set<Student> studentsBelongsToManySet;

    @BelongsToMany(relationModel = RelationshipStudentTeacherModel.class, foreignKeyForLocalModel = "teacher_id", foreignKeyForTargetModel = "student_id", localModelLocalKey = "id", targetModelLocalKey = "id")
    private LinkedHashSet<Student> studentsBelongsToManyLinkedHashSet;

    @BelongsToMany(relationModel = RelationshipStudentTeacherModel.class, foreignKeyForLocalModel = "teacher_id", foreignKeyForTargetModel = "student_id", localModelLocalKey = "id", targetModelLocalKey = "id")
    private LinkedList<Student> studentsBelongsToManyLinkedList;

}
