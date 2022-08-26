package ${namespace};

${imports}

@Data
public abstract class ${base_entity_name} implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** auto generator start **/

${static_fields}
${fields}
    /** auto generator end **/

${base_model_within_base_entity}
}