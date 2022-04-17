package gaarason.database.contract.support;

import gaarason.database.contract.eloquent.Model;

import java.util.Set;

/**
 * 反射扫描器
 * @author xt
 */
public interface ReflectionScan {

    /**
     * 扫描所有model
     * @return model的集合
     */
    Set<Class<? extends Model<?, ?>>> scanModels();

}
