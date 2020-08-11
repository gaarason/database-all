package gaarason.database.test.relation.data.pojo;

import gaarason.database.eloquent.annotations.BelongsTo;
import gaarason.database.eloquent.annotations.Column;
import gaarason.database.eloquent.annotations.Primary;
import gaarason.database.eloquent.annotations.Table;
import gaarason.database.test.relation.data.model.StudentModel;
import gaarason.database.test.relation.data.model.TeacherModel;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@Table(name = "relationship_student_teacher")
public class RelationshipStudentTeacher implements Serializable {

    final public static String ID         = "id";

    final public static String STUDENT_ID = "student_id";

    final public static String TEACHER_ID = "teacher_id";

    final public static String CREATED_AT = "created_at";

    final public static String UPDATED_AT = "updated_at";

    @Primary()
    @Column(name = "id", unsigned = true)
    private Long id;

    @Column(name = "student_id", unsigned = true, comment = "学生id")
    private Long studentId;

    @Column(name = "teacher_id", unsigned = true, comment = "教师id")
    private Long teacherId;

    @Column(name = "created_at", insertable = false, updatable = false, comment = "新增时间")
    private Date createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false, comment = "更新时间")
    private Date updatedAt;



    @BelongsTo(parentModel = StudentModel.class, localModelForeignKey = "student_id")
    private Student students;

    @BelongsTo(parentModel = TeacherModel.class, localModelForeignKey = "teacher_id")
    private Teacher teachers;

}
