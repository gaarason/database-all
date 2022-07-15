package gaarason.database.test.models.normal;

import gaarason.database.annotation.Column;
import gaarason.database.annotation.Primary;
import gaarason.database.annotation.Table;
import gaarason.database.appointment.FieldStrategy;
import gaarason.database.test.models.normal.base.SingleModel;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@Table(name = "student")
public class StudentReversal implements Serializable {

    private static final long serialVersionUID = 1L;

    @Primary
    private Integer id;

    @Column(length = 20)
    private String name;

    private Byte age;

    private Byte sex;

    @Column(name = "teacher_id")
    private Integer teacherId;

    @Column(name = "created_at", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Date createdAt;

    @Column(name = "updated_at", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Date updatedAt;

    public static class Model extends SingleModel<StudentReversal, Integer>{}

}
