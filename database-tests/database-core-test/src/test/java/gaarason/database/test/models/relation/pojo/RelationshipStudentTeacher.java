package gaarason.database.test.models.relation.pojo;

import gaarason.database.eloquent.annotation.BelongsTo;
import gaarason.database.eloquent.annotation.Column;
import gaarason.database.eloquent.annotation.Table;
import gaarason.database.test.models.relation.pojo.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@Table(name = "relationship_student_teacher")
public class RelationshipStudentTeacher extends BaseEntity implements Serializable {

    final public static String STUDENT_ID = "student_id";
    final public static String TEACHER_ID = "teacher_id";
    final public static String NOTE = "note";
    final public static String CREATED_AT = "created_at";
    final public static String UPDATED_AT = "updated_at";

    @Column(name = "student_id", unsigned = true, comment = "学生id")
    private Long studentId;

    @Column(name = "teacher_id", unsigned = true, comment = "教师id")
    private Long teacherId;

    @Column(name = "note", comment = "备注")
    private String note;

    @Column(name = "created_at", insertable = false, updatable = false, comment = "新增时间")
    private Date createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false, comment = "更新时间")
    private Date updatedAt;

    @BelongsTo(localModelForeignKey = "student_id")
    private Student student;

    @BelongsTo(localModelForeignKey = "teacher_id")
    private Teacher teacher;

}
