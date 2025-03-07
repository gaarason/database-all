package gaarason.database.eloquent;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.lang.Nullable;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.support.ModelMember;

import java.util.concurrent.ExecutorService;

/**
 * 数据模型对象
 * @author xt
 */
abstract class ModelBase<T, K> implements Model<T, K> {

    /**
     * Model信息大全
     * 注:不需要volatile修饰
     */
    @Nullable
    protected transient ModelShadowProvider modelShadow;

    /**
     * @return dataSource代理
     */
    public abstract GaarasonDataSource getGaarasonDataSource();

    public ExecutorService getExecutorService(){
        return getContainer().getBean(ExecutorService.class);
    }


    @Override
    public Model<T, K> getSelf() {
        return this;
    }

    /**
     * Model信息
     * @return ModelShadow
     */
    protected ModelShadowProvider getModelShadow() {
        ModelShadowProvider localModelShadow = modelShadow;
        if (localModelShadow == null) {
            synchronized (this) {
                localModelShadow = modelShadow;
                if (localModelShadow == null) {
                    modelShadow = localModelShadow = getGaarasonDataSource().getContainer()
                        .getBean(ModelShadowProvider.class);
                }
            }
        }
        return localModelShadow;
    }

    /**
     * 获取模型信息
     * @return 模型信息
     */
    protected ModelMember<T, K> getModelMember() {
        return getModelShadow().get(this);
    }

}
