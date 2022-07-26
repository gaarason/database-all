package gaarason.database.test.models.normal;

import gaarason.database.annotation.Column;
import gaarason.database.annotation.Primary;
import gaarason.database.annotation.Table;
import gaarason.database.contract.support.FieldStrategy;
import gaarason.database.test.models.normal.base.SingleModel;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@Table(name = "student")
public class StudentCombination extends SingleModel<StudentCombination, Integer> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Primary
    private Integer id;

    @Column(length = 20)
    private String name;

    private Byte age;

    private Byte sex;

    @Column(name = "teacher_id")
    private Integer teacherId;

    @Column(name = "created_at", insertStrategy = FieldStrategy.Never.class, updateStrategy = FieldStrategy.Never.class)
    private Date createdAt;

    @Column(name = "updated_at", insertStrategy = FieldStrategy.Never.class, updateStrategy = FieldStrategy.Never.class)
    private Date updatedAt;

}
