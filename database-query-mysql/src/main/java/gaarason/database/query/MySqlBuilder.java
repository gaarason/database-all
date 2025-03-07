package gaarason.database.query;

/**
 * Mysql sql生成器
 * @param <T>
 * @param <K>
 * @author xt
 */
public final class MySqlBuilder<T, K> extends AbstractBuilder<MySqlBuilder<T, K>, T, K> {

    private static final long serialVersionUID = 1L;

    @Override
    public MySqlBuilder<T, K> getSelf() {
        return this;
    }

}
