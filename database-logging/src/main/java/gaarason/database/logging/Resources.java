package gaarason.database.logging;
import java.security.AccessController;
import java.security.PrivilegedAction;


/**
 * A class to simplify access to resources through the classloader.
 */
public final class Resources {

    private static ClassLoader defaultClassLoader;

    private Resources() {
    }

    /**
     * Returns the default classloader (may be null).
     * @return The default classloader
     */
    public static ClassLoader getDefaultClassLoader() {
        return defaultClassLoader;
    }

    /**
     * Sets the default classloader
     * @param defaultClassLoader - the new default ClassLoader
     */
    public static void setDefaultClassLoader(ClassLoader defaultClassLoader) {
        Resources.defaultClassLoader = defaultClassLoader;
    }

    /**
     * Loads a class
     * @param className - the class to load
     * @return The loaded class
     * @throws ClassNotFoundException If the class cannot be found (duh!)
     */
    public static Class<?> classForName(String className) throws ClassNotFoundException {
        Class<?> clazz = null;
        try {
            clazz = getClassLoader().loadClass(className);
        } catch (Exception e) {
            // Ignore. Failsafe below.
        }
        if (clazz == null) {
            clazz = Class.forName(className);
        }
        return clazz;
    }

    private static ClassLoader getClassLoader() {
        // Keep original behavior if default class loader is set
        if (defaultClassLoader != null) {
            return defaultClassLoader;
        }
        
        // Check if security manager is present (optimization from fixed code)
        if (System.getSecurityManager() == null) {
            // Fast path when no security manager exists
            return Thread.currentThread().getContextClassLoader();
        } else {
            // Use doPrivileged when security manager is active
            return AccessController.doPrivileged((PrivilegedAction<ClassLoader>) () -> {
                try {
                    return Thread.currentThread().getContextClassLoader();
                } catch (SecurityException ex) {
                    // Log exception but don't expose stack trace
                    // Using System.err since we don't want to assume logger availability
                    System.err.println("SecurityException: Unable to access thread context class loader");
                    // Return null on failure, maintaining original behavior on exception
                    return null;
                }
            });
        }
    }

}
