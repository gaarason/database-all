package gaarason.database.logging;

import java.lang.reflect.Constructor;

@SuppressWarnings("rawtypes")
public class LogFactory {

    private static Constructor logConstructor;

    static {
        String logType = System.getProperty("gaarason.database.logType");
        if (logType != null) {
            if (logType.equalsIgnoreCase("slf4j")) {
                tryImplementation("org.slf4j.Logger", "gaarason.database.logging.SLF4JImpl");
            } else if (logType.equalsIgnoreCase("log4j")) {
                tryImplementation("org.apache.log4j.Logger", "gaarason.database.logging.Log4jImpl");
            } else if (logType.equalsIgnoreCase("log4j2")) {
                tryImplementation("org.apache.logging.log4j.Logger", "gaarason.database.logging.Log4j2Impl");
            } else if (logType.equalsIgnoreCase("commonsLog")) {
                tryImplementation("org.apache.commons.logging.LogFactory", "gaarason.database.logging.JakartaCommonsLoggingImpl");
            } else if (logType.equalsIgnoreCase("jdkLog")) {
                tryImplementation("java.util.logging.Logger", "gaarason.database.logging.Jdk14LoggingImpl");
            }
        }
        // 优先选择log4j,而非Apache Common Logging. 因为后者无法设置真实Log调用者的信息
        tryImplementation("org.slf4j.Logger", "gaarason.database.logging.SLF4JImpl");
        tryImplementation("org.apache.log4j.Logger", "gaarason.database.logging.Log4jImpl");
        tryImplementation("org.apache.logging.log4j.Logger", "gaarason.database.logging.Log4j2Impl");
        tryImplementation("org.apache.commons.logging.LogFactory", "gaarason.database.logging.JakartaCommonsLoggingImpl");
        tryImplementation("java.util.logging.Logger", "gaarason.database.logging.Jdk14LoggingImpl");

        if (logConstructor == null) {
            try {
                logConstructor = NoLoggingImpl.class.getConstructor(String.class);
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void tryImplementation(String testClassName, String implClassName) {
        if (logConstructor != null) {
            return;
        }

        try {
            Resources.classForName(testClassName);
            Class implClass = Resources.classForName(implClassName);
            logConstructor = implClass.getConstructor(String.class);

            Class<?> declareClass = logConstructor.getDeclaringClass();
            if (!Log.class.isAssignableFrom(declareClass)) {
                logConstructor = null;
            }

            try {
                if (null != logConstructor) {
                    logConstructor.newInstance(LogFactory.class.getName());
                }
            } catch (Throwable t) {
                logConstructor = null;
            }

        } catch (Throwable t) {
            // skip
        }
    }

    public static Log getLog(Class clazz) {
        return getLog(clazz.getName());
    }

    public static Log getLog(String loggerName) {
        try {
            return (Log) logConstructor.newInstance(loggerName);
        } catch (Throwable t) {
            throw new RuntimeException("Error creating logger for logger '" + loggerName + "'.  Cause: " + t, t);
        }
    }
}
