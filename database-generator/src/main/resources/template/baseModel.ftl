package ${namespace};
    
import ${base_entity_namespace}.${base_entity_name};
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.eloquent.Model;
import gaarason.database.lang.NonNull;
import gaarason.database.query.MySqlBuilder;
import lombok.extern.slf4j.Slf4j;
${imports}
    
import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Collection;
    
@Slf4j
public abstract class ${base_model_name}<T extends ${base_entity_name}, K extends Serializable> extends Model<MySqlBuilder<T, K>, T, K> {

    ${spring_lazy}
    @Resource
    protected GaarasonDataSource gaarasonDataSource;
    
    @NonNull
    @Override
    public GaarasonDataSource getGaarasonDataSource(){
    return gaarasonDataSource;
    }
    
    /**
     * sql日志记录
     * @param sql           带占位符的sql
     * @param parameterList 参数
     */
    public void log(String sql, Collection<?> parameterList) {
    // String format = String.format(sql.replace(" ? ", "\"%s\""), parameterList.toArray());
    // log.debug("SQL complete : {}", format);
    }
    
}