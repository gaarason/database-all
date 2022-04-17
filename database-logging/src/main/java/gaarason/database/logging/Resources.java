package gaarason.database.logging;


/**
 * A class to simplify access to resources through the classloader.
 */
public final class Resources extends Object {

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
        if (defaultClassLoader != null) {
            return defaultClassLoader;
        } else {
            return Thread.currentThread().getContextClassLoader();
        }
    }

}
