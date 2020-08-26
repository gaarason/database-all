package gaarason.database.query.grammars;

import gaarason.database.contracts.Grammar;
import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.contracts.function.RelationshipRecordWith;
import gaarason.database.exception.CloneNotSupportedRuntimeException;
import gaarason.database.utils.ObjectUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

abstract public class BaseGrammar implements Grammar, Serializable {

    /**
     * column -> [ GenerateSqlPart , RelationshipRecordWith ]
     */
    private Map<String, Object[]> withMap = new HashMap<>();

    /**
     * 记录with信息
     * @param column         所关联的Model(当前模块的属性名)
     * @param builderClosure 所关联的Model的查询构造器约束
     * @param recordClosure  所关联的Model的再一级关联
     */
    @Override
    public void pushWith(String column, GenerateSqlPart builderClosure, RelationshipRecordWith recordClosure) {
        withMap.put(column, new Object[]{builderClosure, recordClosure});
    }

    /**
     * 拉取with信息
     * @return map
     */
    @Override
    public Map<String, Object[]> pullWith() {
        return withMap;
    }

    /**
     * 深度copy
     * @return 和当前属性值一样的全新对象
     * @throws CloneNotSupportedRuntimeException 克隆异常
     */
    @Override
    public Grammar deepCopy() throws CloneNotSupportedRuntimeException {
        // 暂存
        Map<String, Object[]> withMapTemp = withMap;
        // 移除
        withMap = null;
        // 拷贝
        BaseGrammar baseGrammar = ObjectUtil.deepCopy(this);
        // 还原
        baseGrammar.withMap = withMap = withMapTemp;
        return baseGrammar;
    }
}
