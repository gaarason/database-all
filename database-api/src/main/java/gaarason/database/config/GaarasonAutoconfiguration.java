package gaarason.database.config;

import gaarason.database.core.Container;

/**
 * 自动配置类
 * 实现这个接口的类, 会被包扫描检测, 并执行init()方法
 * @author xt
 */
@FunctionalInterface
public interface GaarasonAutoconfiguration {

    void init(Container container);
}
