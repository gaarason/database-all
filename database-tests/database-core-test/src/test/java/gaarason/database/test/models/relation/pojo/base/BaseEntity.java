package gaarason.database.test.models.relation.pojo.base;

import gaarason.database.annotation.Column;
import gaarason.database.annotation.Primary;
import lombok.Data;

import java.io.Serializable;

@Data
abstract public class BaseEntity implements Serializable {
    /** auto generator start **/

    final public static String ID = "id";
    private static final long serialVersionUID = 1L;
    @Primary()
    @Column(name = "id", unsigned = true)
    private Long id;


    /** auto generator end **/
}