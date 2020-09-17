package gaarason.database.test.relation.data.pojo;

import gaarason.database.eloquent.annotation.*;
import gaarason.database.test.relation.data.model.RelationshipStudentTeacherModel;
import gaarason.database.test.relation.data.model.TeacherModel;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Table(name = "student")
public class Student implements Serializable {

    final public static String ID = "id";

    final public static String NAME = "name";

    final public static String AGE = "age";

    final public static String SEX = "sex";

    final public static String TEACHER_ID = "teacher_id";

    final public static String IS_DELETED = "is_deleted";

    final public static String CREATED_AT = "created_at";

    final public static String UPDATED_AT = "updated_at";

    @Primary()
    @Column(name = "id", unsigned = true)
    private Long id;

    @Column(name = "name", length = 20, comment = "姓名")
    private String name;

    @Column(name = "age", unsigned = true, comment = "年龄")
    private Integer age;

    @Column(name = "sex", unsigned = true, comment = "性别1男2女")
    private Integer sex;

    @Column(name = "teacher_id", unsigned = true, comment = "教师id")
    private Long teacherId;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "created_at", insertable = false, updatable = false, comment = "新增时间")
    private Date createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false, comment = "更新时间")
    private Date updatedAt;

    @BelongsTo(localModelForeignKey = "teacher_id", parentModelLocalKey = "id")
    private Teacher teacher;

    @HasOneOrMany(sonModelForeignKey = "student_id")
    private List<RelationshipStudentTeacher> relationshipStudentTeachers;


    @BelongsToMany(relationModel = RelationshipStudentTeacherModel.class,
        foreignKeyForLocalModel = "student_id", foreignKeyForTargetModel = "teacher_id", localModelLocalKey = "id",
        targetModelLocalKey = "id")
    private List<Teacher> teachersBelongsToMany;


}
