package gaarason.database.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

public class SLF4JImpl implements Log {

    private static final String CALLER_FQCN = SLF4JImpl.class.getName();

    private static final Logger TEST_LOGGER = LoggerFactory.getLogger(SLF4JImpl.class);

    static {
        // if the logger is not a LocationAwareLogger instance, it can not get correct stack StackTraceElement
        // so ignore this implementation.
        if (!(TEST_LOGGER instanceof LocationAwareLogger)) {
            throw new UnsupportedOperationException(TEST_LOGGER.getClass() + " is not a suitable logger");
        }
    }

    private final LocationAwareLogger log;
    private int errorCount;
    private int warnCount;
    private int infoCount;
    private int debugCount;

    public SLF4JImpl(LocationAwareLogger log) {
        this.log = log;
    }

    public SLF4JImpl(String loggerName) {
        this.log = (LocationAwareLogger) LoggerFactory.getLogger(loggerName);
    }

    @Override
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public void error(String msg, Throwable e) {
        log.log(null, CALLER_FQCN, LocationAwareLogger.ERROR_INT, msg, null, e);
        errorCount++;
    }

    @Override
    public void error(String msg) {
        log.log(null, CALLER_FQCN, LocationAwareLogger.ERROR_INT, msg, null, null);
        errorCount++;
    }

    @Override
    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        infoCount++;
        log.log(null, CALLER_FQCN, LocationAwareLogger.INFO_INT, msg, null, null);
    }

    @Override
    public void debug(String msg) {
        debugCount++;
        log.log(null, CALLER_FQCN, LocationAwareLogger.DEBUG_INT, msg, null, null);
    }

    @Override
    public void debug(String msg, Throwable e) {
        debugCount++;
        log.log(null, CALLER_FQCN, LocationAwareLogger.ERROR_INT, msg, null, e);
    }

    @Override
    public boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    @Override
    public void warn(String msg) {
        log.log(null, CALLER_FQCN, LocationAwareLogger.WARN_INT, msg, null, null);
        warnCount++;
    }

    @Override
    public void warn(String msg, Throwable e) {
        log.log(null, CALLER_FQCN, LocationAwareLogger.WARN_INT, msg, null, e);
        warnCount++;
    }

    @Override
    public int getErrorCount() {
        return errorCount;
    }

    @Override
    public int getWarnCount() {
        return warnCount;
    }

    @Override
    public int getInfoCount() {
        return infoCount;
    }

    public int getDebugCount() {
        return debugCount;
    }

    @Override
    public void resetStat() {
        errorCount = 0;
        warnCount = 0;
        infoCount = 0;
        debugCount = 0;
    }

}
