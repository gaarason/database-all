package gaarason.database.contract.builder;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.support.LambdaStyle;
import gaarason.database.lang.Nullable;

import java.util.Collection;

/**
 * 特殊
 * @author xt
 */
public interface Special<B extends Builder<B, T, K>, T, K> extends LambdaStyle, Cloneable {

    /**
     * 拼接在sql尾部的原始sql
     * @param sqlPart sql片段
     * @return 查询构造器
     */
    B lastRaw(String sqlPart);

    /**
     * 拼接在sql尾部的原始sql
     * @param sqlPart sql片段
     * @param parameters sql绑定参数
     * @return 查询构造器
     */
    B lastRaw(String sqlPart, @Nullable Collection<Object> parameters);
}
