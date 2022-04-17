package gaarason.database.spring.boot.starter.test.data.entity;

import gaarason.database.annotation.*;
import gaarason.database.spring.boot.starter.test.data.model.RelationshipStudentTeacherModel;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Table(name = "teacher")
public class Teacher implements Serializable {

    /**
     * auto generator start
     **/

    final public static String ID = "id";

    final public static String NAME = "name";

    final public static String AGE = "age";

    final public static String SEX = "sex";

    final public static String SUBJECT = "subject";

    final public static String CREATED_AT = "created_at";

    final public static String UPDATED_AT = "updated_at";

    @Primary()
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", length = 20, comment = "姓名")
    private String name;

    @Column(name = "age", unsigned = true, comment = "年龄")
    private Integer age;

    @Column(name = "sex", unsigned = true, comment = "性别1男2女")
    private Integer sex;

    @Column(name = "subject", length = 20, comment = "科目")
    private String subject;

    @Column(name = "created_at", insertable = false, updatable = false, comment = "新增时间")
    private Date createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false, comment = "更新时间")
    private Date updatedAt;

    /**
     * auto generator end
     **/

    @HasOneOrMany(sonModelForeignKey = "teacher_id")
    private List<Student> students;

    @HasOneOrMany(sonModelForeignKey = "teacher_id", localModelLocalKey = "id")
    private Student student;

    @HasOneOrMany(sonModelForeignKey = "teacher_id")
    private List<RelationshipStudentTeacher> relationshipStudentTeachers;

    @BelongsToMany(relationModel = RelationshipStudentTeacherModel.class,
        foreignKeyForLocalModel = "teacher_id", foreignKeyForTargetModel = "student_id", localModelLocalKey = "id",
        targetModelLocalKey = "id")
    private List<Student> studentsBelongsToMany;
}