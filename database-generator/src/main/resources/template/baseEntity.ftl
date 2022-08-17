package ${namespace};

import gaarason.database.annotation.Column;
import gaarason.database.annotation.Primary;
${swagger_import}
import lombok.Data;
${validator_import}
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
    
@Data
public abstract class ${base_entity_name} implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** auto generator start **/

${static_fields}
${fields}
    /** auto generator end **/
}