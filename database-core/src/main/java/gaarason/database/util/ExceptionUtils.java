package gaarason.database.util;

/**
 * 异常
 * @author xt
 */
public class ExceptionUtils {

    private ExceptionUtils() {
    }

    /**
     * 是否为数据库死锁导致的异常
     * @param e 原异常
     * @return 是否为数据库死锁导致的异常
     */
    public static boolean causedByDeadlock(Throwable e) {
        String message = e.getMessage();
        if (message == null) {
            return false;
        }
        String[] exceptionMessages = {
            "Deadlock found when trying to get lock",
            "deadlock detected",
            "The database file is locked",
            "database is locked",
            "database table is locked",
            "A table in the database is locked",
            "has been chosen as the deadlock victim",
            "Lock wait timeout exceeded; try restarting transaction",
            "WSREP detected deadlock/conflict and aborted the transaction. Try restarting the transaction",
        };
        for (String exceptionMessage : exceptionMessages)
            if (message.contains(exceptionMessage))
                return true;
        return false;
    }
}
