    public abstract static class ${base_model_name}<T extends ${base_entity_name}, K> extends Model<MySqlBuilder<T, K>, T, K> {

        ${spring_lazy}
        @Resource
        protected GaarasonDataSource gaarasonDataSource;

        @Override
        public GaarasonDataSource getGaarasonDataSource(){
            return gaarasonDataSource;
        }

        /**
         * sql日志记录
         * @param sql           带占位符的sql
         * @param parameterList 参数
         */
         @Override
         public void log(String sql,Collection<?> parameterList){
            // String format = String.format(sql.replace(" ? ", "\"%s\""), parameterList.toArray());
            // log.debug("SQL complete : {}", format);
        }

    }