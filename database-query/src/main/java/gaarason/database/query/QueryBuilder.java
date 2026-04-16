package gaarason.database.query;

/**
 * 数据库无关的查询构造器, 框架默认提供的具体Builder实现
 * <p>
 * 所有方言差异通过Grammar处理, 本类可与任意数据库配合使用
 * @param <T> 实体类型
 * @param <K> 主键类型
 * @author xt
 */
public class QueryBuilder<T, K> extends AbstractBuilder<QueryBuilder<T, K>, T, K> {

    private static final long serialVersionUID = 1L;

    @Override
    public QueryBuilder<T, K> getSelf() {
        return this;
    }

}
