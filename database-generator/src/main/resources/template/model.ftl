package ${namespace};

import ${base_model_namespace}.${base_model_name};
import ${entity_namespace}.${entity_name};
${imports}
import java.io.Serializable;

${is_spring_boot}
public class ${model_name} extends ${base_model_name}<${entity_name}, ${primary_key_type}> {

}