package ${namespace};

${imports}

${swagger_annotation}
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "${table}")
public class ${entity_name} extends ${base_entity_name} {
    private static final long serialVersionUID = 1L;

    /** auto generator start **/

${static_fields}
${fields}
    /** auto generator end **/

${model_within_entity}

}