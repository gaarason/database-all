public abstract class ${model_name}<T extends ${base_entity_name}, K extends Serializable> extends Model<T, K> {

    @Resource
    protected GaarasonDataSource gaarasonDataSource;

    @NonNull
    @Override
    protected GaarasonDataSource getGaarasonDataSource(){
    return gaarasonDataSource;
    }

    /**
     * sql日志记录
     * @param sql           带占位符的sql
     * @param parameterList 参数
     */
    public void log(String sql, Collection<String> parameterList) {
    // String format = String.format(sql.replace(" ? ", "\"%s\""), parameterList.toArray());
    // log.debug("SQL complete : {}", format);
    }


}