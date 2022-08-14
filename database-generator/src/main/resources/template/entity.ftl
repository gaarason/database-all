package ${namespace};

import ${base_entity_namespace}.${base_entity_name};
import gaarason.database.annotation.Column;
import gaarason.database.annotation.Primary;
import gaarason.database.annotation.Table;
${swagger_import}
import lombok.Data;
import lombok.EqualsAndHashCode;
${validator_import}
import java.io.Serializable;
import java.math.BigInteger;
import java.time.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "${table}")
${swagger_annotation}
public class ${entity_name} extends ${base_entity_name} {
    private static final long serialVersionUID = 1L;

    /** auto generator start **/

${static_fields}
${fields}
    /** auto generator end **/
}