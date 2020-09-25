package gaarason.database.contract.support;

import gaarason.database.contract.eloquent.Model;

import java.util.Set;

/**
 * 反射扫描器
 */
public interface ReflectionScan {

    Set<Class<? extends Model<?, ?>>> scanModels();

}
