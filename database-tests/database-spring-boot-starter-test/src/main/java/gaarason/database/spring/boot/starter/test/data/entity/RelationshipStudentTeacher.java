package gaarason.database.spring.boot.starter.test.data.entity;

import gaarason.database.annotation.BelongsTo;
import gaarason.database.annotation.Column;
import gaarason.database.annotation.Primary;
import gaarason.database.annotation.Table;
import gaarason.database.contract.support.FieldStrategy;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@Table(name = "relationship_student_teacher")
public class RelationshipStudentTeacher implements Serializable {

    /**
     * auto generator start
     **/

    final public static String ID = "id";

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

    @Column(name = "created_at", insertStrategy = FieldStrategy.Never.class, updateStrategy = FieldStrategy.Never.class, comment = "新增时间")
    private Date createdAt;

    private String note;

    @Column(name = "updated_at", insertStrategy = FieldStrategy.Never.class, updateStrategy = FieldStrategy.Never.class, comment = "更新时间")
    private Date updatedAt;

    /** auto generator end **/

    @BelongsTo(localModelForeignKey = "student_id")
    private Student student;

    @BelongsTo(localModelForeignKey = "teacher_id")
    private Teacher teacher;
}