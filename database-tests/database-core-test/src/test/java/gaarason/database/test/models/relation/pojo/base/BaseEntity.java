package gaarason.database.test.models.relation.pojo.base;

import gaarason.database.eloquent.annotation.Column;
import gaarason.database.eloquent.annotation.Primary;
import lombok.Data;

import java.io.Serializable;

@Data
abstract public class BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** auto generator start **/

    final public static String ID = "id";

    @Primary()
    @Column(name = "id", unsigned = true)
    private Long id;


    /** auto generator end **/
}