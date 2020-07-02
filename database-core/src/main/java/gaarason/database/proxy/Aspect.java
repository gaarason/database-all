package gaarason.database.proxy;

public interface Aspect {

//    void addAction(Class<?> object, ActionEnum actionEnum, String MethodName, Runnable closure);

    void before(Object objectEntity, String MethodName, Object[] argArray);

    void afterReturning(Object objectEntity, String MethodName, Object[] argArray);

    void afterThrowing(Object objectEntity, String MethodName, Object[] argArray);

    void afterFinally(Object objectEntity, String MethodName, Object[] argArray);

}
