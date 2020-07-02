package gaarason.database.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyGenerator implements InvocationHandler {
    private Object target;

    public ProxyGenerator(Object object) {
        this.target = object;
    }

    @Override
    public Object invoke(Object arg0, Method arg1, Object[] arg2)
        throws Throwable {
        MonitorSession.begin(target, arg1.getName());
        Object obj = arg1.invoke(target, arg2);
        MonitorSession.end();
        return obj;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxy() {
        return (T) Proxy.newProxyInstance(
            target.getClass().getClassLoader(),
            target.getClass().getInterfaces(),
            this
        );
    }
}