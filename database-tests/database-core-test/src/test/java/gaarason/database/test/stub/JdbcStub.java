package gaarason.database.test.stub;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * JDBC 桩实现.
 * <p>
 * 用于在不连接真实数据库的情况下, 记录并回放 SQL 执行过程:
 * 1. 记录每一条预执行语句的 sql、绑定参数、是否被关闭;
 * 2. 按脚本回放查询结果行(列名 -> 值);
 * 3. 方言探测时返回固定的数据库产品名.
 */
public class JdbcStub implements DataSource {

    /**
     * 数据库产品名, 用于方言探测
     */
    public String databaseProductName = "MySQL";

    /**
     * 写操作返回的影响行数
     */
    public int updateCount = 1;

    /**
     * 创建过的全部预执行语句, 按创建顺序
     */
    public final List<StubStatement> statements = Collections.synchronizedList(new ArrayList<>());

    /**
     * 关闭连接次数
     */
    public int connectionClosed = 0;

    /**
     * 待回放的查询结果队列, 每次 executeQuery 取走一组
     */
    private final List<List<Map<String, Object>>> scriptedRows =
        Collections.synchronizedList(new ArrayList<>());

    /**
     * 记录的一条预执行语句
     */
    public static class StubStatement {

        /**
         * 下发的 sql
         */
        final public String sql;

        /**
         * 绑定参数, 按占位符索引(1-based)
         */
        final public Map<Integer, Object> parameters = new TreeMap<>();

        /**
         * 是否被执行过
         */
        public boolean executed;

        /**
         * 是否被关闭
         */
        public boolean closed;

        StubStatement(String sql) {
            this.sql = sql;
        }

        /**
         * 按占位符顺序返回绑定参数
         * @return 参数列表
         */
        public List<Object> parameterList() {
            return new ArrayList<>(parameters.values());
        }

        @Override
        public String toString() {
            return sql + " <" + parameterList() + "> " + (closed ? "[closed]" : "[open]");
        }
    }

    /**
     * 追加一组待回放的查询结果
     * @param rows 行数据, 列名 -> 值
     */
    public void enqueueRows(Map<String, Object>... rows) {
        scriptedRows.add(new ArrayList<>(Arrays.asList(rows)));
    }

    /**
     * 清空记录
     */
    public void reset() {
        statements.clear();
        scriptedRows.clear();
        connectionClosed = 0;
    }

    /**
     * 未关闭的语句
     * @return 未关闭的语句列表
     */
    public List<StubStatement> openStatements() {
        List<StubStatement> open = new ArrayList<>();
        for (StubStatement statement : new ArrayList<>(statements)) {
            if (!statement.closed) {
                open.add(statement);
            }
        }
        return open;
    }

    /**
     * 匹配 sql 前缀的语句
     * @param prefix sql 前缀
     * @return 语句列表
     */
    public List<StubStatement> statementsStartingWith(String prefix) {
        List<StubStatement> matched = new ArrayList<>();
        for (StubStatement statement : new ArrayList<>(statements)) {
            if (statement.sql.startsWith(prefix)) {
                matched.add(statement);
            }
        }
        return matched;
    }

    @Override
    public Connection getConnection() {
        return newConnection();
    }

    @Override
    public Connection getConnection(String username, String password) {
        return newConnection();
    }

    /**
     * 构造一个连接桩
     * @return 连接
     */
    public Connection newConnection() {
        JdbcStub self = this;
        return (Connection) Proxy.newProxyInstance(JdbcStub.class.getClassLoader(),
            new Class<?>[]{Connection.class}, new ConnectionHandler(self));
    }

    @Override
    public PrintWriter getLogWriter() {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) {

    }

    @Override
    public void setLoginTimeout(int seconds) {

    }

    @Override
    public int getLoginTimeout() {
        return 0;
    }

    @Override
    public Logger getParentLogger() {
        return Logger.getLogger("jdbc-stub");
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException("not a wrapper");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return false;
    }

    /**
     * 连接处理器
     */
    private static class ConnectionHandler implements InvocationHandler {

        final private JdbcStub stub;

        private Connection proxy;

        ConnectionHandler(JdbcStub stub) {
            this.stub = stub;
        }

        @Override
        public Object invoke(Object target, Method method, Object[] args) {
            this.proxy = (Connection) target;
            return handle(method, args);
        }

        private Object handle(Method method, Object[] args) {
            String name = method.getName();
            switch (name) {
                case "equals":
                    return proxy == args[0];
                case "hashCode":
                    return System.identityHashCode(proxy);
                case "toString":
                    return "JdbcStubConnection";
                case "prepareStatement":
                case "prepareCall": {
                    StubStatement recorded = new StubStatement(String.valueOf(args[0]));
                    stub.statements.add(recorded);
                    Class<?>[] interfaces = "prepareCall".equals(name)
                        ? new Class<?>[]{java.sql.CallableStatement.class, PreparedStatement.class}
                        : new Class<?>[]{PreparedStatement.class};
                    return Proxy.newProxyInstance(JdbcStub.class.getClassLoader(), interfaces,
                        new StatementHandler(stub, recorded, proxy));
                }
                case "createStatement": {
                    StubStatement recorded = new StubStatement("");
                    stub.statements.add(recorded);
                    return Proxy.newProxyInstance(JdbcStub.class.getClassLoader(),
                        new Class<?>[]{Statement.class}, new StatementHandler(stub, recorded, proxy));
                }
                case "getMetaData":
                    return Proxy.newProxyInstance(JdbcStub.class.getClassLoader(),
                        new Class<?>[]{DatabaseMetaData.class},
                        (metadataTarget, metadataMethod, metadataArgs) -> {
                            switch (metadataMethod.getName()) {
                                case "getDatabaseProductName":
                                    return stub.databaseProductName;
                                case "getDatabaseProductVersion":
                                case "getDriverVersion":
                                    return "stub";
                                case "getURL":
                                    return "jdbc:stub://localhost/stub";
                                case "getUserName":
                                    return "stub";
                                case "equals":
                                    return metadataTarget == metadataArgs[0];
                                case "hashCode":
                                    return System.identityHashCode(metadataTarget);
                                case "toString":
                                    return "JdbcStubMetaData";
                                default:
                                    return defaultValue(metadataMethod.getReturnType());
                            }
                        });
                case "close":
                    stub.connectionClosed++;
                    return null;
                case "isClosed":
                    return false;
                case "isValid":
                    return true;
                case "getAutoCommit":
                    return true;
                case "getCatalog":
                case "getSchema":
                    return "stub";
                case "nativeSQL":
                    return args[0];
                case "unwrap":
                    return proxy;
                case "isWrapperFor":
                    return false;
                default:
                    return defaultValue(method.getReturnType());
            }
        }
    }

    /**
     * 语句处理器
     */
    private static class StatementHandler implements InvocationHandler {

        final private JdbcStub stub;

        final private StubStatement recorded;

        final private Connection connection;

        private Object proxy;

        StatementHandler(JdbcStub stub, StubStatement recorded, Connection connection) {
            this.stub = stub;
            this.recorded = recorded;
            this.connection = connection;
        }

        @Override
        public Object invoke(Object target, Method method, Object[] args) {
            if (target != proxy) {
                this.proxy = target;
            }
            String name = method.getName();
            if ("equals".equals(name)) {
                return proxy == args[0];
            }
            if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            }
            if ("toString".equals(name)) {
                return "JdbcStubStatement(" + recorded.sql + ")";
            }

            // 参数绑定: setXxx(索引, 值, ...)
            if (name.startsWith("set") && args != null && args.length >= 2 && args[0] instanceof Integer) {
                if ("setNull".equals(name)) {
                    recorded.parameters.put((Integer) args[0], null);
                } else {
                    recorded.parameters.put((Integer) args[0], args[1]);
                }
                return defaultValue(method.getReturnType());
            }
            if ("clearParameters".equals(name)) {
                recorded.parameters.clear();
                return null;
            }
            if ("close".equals(name)) {
                recorded.closed = true;
                return null;
            }
            if ("executeQuery".equals(name)) {
                recorded.executed = true;
                return newResultSet(stub);
            }
            if ("getResultSet".equals(name) || "getGeneratedKeys".equals(name)) {
                return newResultSet(stub);
            }
            if (name.startsWith("execute")) {
                recorded.executed = true;
                return "executeQuery".equals(name) ? newResultSet(stub) : stub.updateCount;
            }
            if ("getMoreResults".equals(name)) {
                return false;
            }
            if ("getUpdateCount".equals(name)) {
                return stub.updateCount;
            }
            if ("getConnection".equals(name)) {
                return connection;
            }
            if ("getWarnings".equals(name) || "getRowLimits".equals(name)) {
                return null;
            }
            if ("wasNull".equals(name)) {
                return false;
            }
            if ("unwrap".equals(name)) {
                return proxy;
            }
            if ("isWrapperFor".equals(name)) {
                return false;
            }
            return defaultValue(method.getReturnType());
        }
    }

    /**
     * 取出一组待回放的行
     */
    private static List<Map<String, Object>> pollRows(JdbcStub stub) {
        synchronized (stub.scriptedRows) {
            if (stub.scriptedRows.isEmpty()) {
                return new ArrayList<>();
            }
            return stub.scriptedRows.remove(0);
        }
    }

    /**
     * 结果集桩
     */
    private static ResultSet newResultSet(JdbcStub stub) {
        List<Map<String, Object>> rows = pollRows(stub);
        List<String> columns = new ArrayList<>();
        if (!rows.isEmpty()) {
            columns.addAll(rows.get(0).keySet());
        }
        return (ResultSet) Proxy.newProxyInstance(JdbcStub.class.getClassLoader(),
            new Class<?>[]{ResultSet.class}, new ResultSetHandler(rows, columns));
    }

    /**
     * 结果集处理器
     */
    private static class ResultSetHandler implements InvocationHandler {

        final private List<Map<String, Object>> rows;

        final private List<String> columns;

        private int cursor = -1;

        private boolean lastWasNull;

        private Object proxy;

        ResultSetHandler(List<Map<String, Object>> rows, List<String> columns) {
            this.rows = rows;
            this.columns = columns;
        }

        @Override
        public Object invoke(Object target, Method method, Object[] args) {
            if (target != proxy) {
                this.proxy = target;
            }
            String name = method.getName();
            switch (name) {
                case "equals":
                    return proxy == args[0];
                case "hashCode":
                    return System.identityHashCode(proxy);
                case "toString":
                    return "JdbcStubResultSet" + columns;
                case "next": {
                    cursor++;
                    return cursor < rows.size();
                }
                case "getMetaData":
                    return newMetaData(columns);
                case "close":
                    return null;
                case "wasNull":
                    return lastWasNull;
                case "findColumn":
                    return columns.indexOf(String.valueOf(args[0])) + 1;
                case "getRow":
                    return cursor + 1;
                case "isClosed":
                    return false;
                case "unwrap":
                    return proxy;
                case "isWrapperFor":
                    return false;
                default:
                    break;
            }

            if (args == null || args.length == 0) {
                return defaultValue(method.getReturnType());
            }

            Object value = valueAt(args[0]);
            lastWasNull = value == null;
            return convert(name, value, args, method.getReturnType());
        }

        private Object valueAt(Object locator) {
            if (cursor < 0 || cursor >= rows.size()) {
                return null;
            }
            Map<String, Object> row = rows.get(cursor);
            if (locator instanceof Integer) {
                int index = (Integer) locator - 1;
                if (index < 0 || index >= columns.size()) {
                    return null;
                }
                return row.get(columns.get(index));
            }
            return row.get(String.valueOf(locator));
        }

        private Object convert(String name, Object value, Object[] args, Class<?> returnType) {
            if (value == null) {
                return defaultValue(returnType);
            }
            switch (name) {
                case "getString":
                    return String.valueOf(value);
                case "getInt":
                    return ((Number) value).intValue();
                case "getLong":
                    return ((Number) value).longValue();
                case "getShort":
                    return ((Number) value).shortValue();
                case "getByte":
                    return ((Number) value).byteValue();
                case "getDouble":
                    return ((Number) value).doubleValue();
                case "getFloat":
                    return ((Number) value).floatValue();
                case "getBoolean":
                    return Boolean.valueOf(String.valueOf(value));
                case "getBigDecimal":
                    return new BigDecimal(String.valueOf(value));
                case "getObject":
                    if (args.length >= 2 && args[1] instanceof Class) {
                        Class<?> wanted = (Class<?>) args[1];
                        if (wanted.isInstance(value)) {
                            return value;
                        }
                        if (Number.class.isAssignableFrom(wanted) && value instanceof Number) {
                            return defaultValue(wanted);
                        }
                        if (wanted == String.class) {
                            return String.valueOf(value);
                        }
                    }
                    return value;
                default:
                    return defaultValue(returnType);
            }
        }

        private ResultSetMetaData newMetaData(List<String> columns) {
            return (ResultSetMetaData) Proxy.newProxyInstance(JdbcStub.class.getClassLoader(),
                new Class<?>[]{ResultSetMetaData.class}, (metaTarget, metaMethod, metaArgs) -> {
                    switch (metaMethod.getName()) {
                        case "getColumnCount":
                            return columns.size();
                        case "getColumnLabel":
                        case "getColumnName":
                            return columns.get((Integer) metaArgs[0] - 1);
                        case "getColumnType":
                            return java.sql.Types.INTEGER;
                        case "getColumnTypeName":
                            return "int";
                        case "getColumnClassName":
                            return Object.class.getName();
                        case "isNullable":
                            return ResultSetMetaData.columnNullable;
                        case "equals":
                            return metaTarget == metaArgs[0];
                        case "hashCode":
                            return System.identityHashCode(metaTarget);
                        case "toString":
                            return "JdbcStubResultSetMetaData";
                        default:
                            return defaultValue(metaMethod.getReturnType());
                    }
                });
        }
    }

    /**
     * 基本类型返回值兜底
     */
    private static Object defaultValue(Class<?> returnType) {
        if (returnType == void.class || returnType == Void.class) {
            return null;
        }
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == char.class) {
            return (char) 0;
        }
        if (returnType == byte.class) {
            return (byte) 0;
        }
        if (returnType == short.class) {
            return (short) 0;
        }
        if (returnType == int.class) {
            return 0;
        }
        if (returnType == long.class) {
            return 0L;
        }
        if (returnType == float.class) {
            return 0f;
        }
        if (returnType == double.class) {
            return 0d;
        }
        return null;
    }

    /**
     * 便捷构造一行数据
     * @param columnValues 列名与值交替给出
     * @return 行
     */
    public static Map<String, Object> row(Object... columnValues) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 0; i + 1 < columnValues.length; i += 2) {
            row.put(String.valueOf(columnValues[i]), columnValues[i + 1]);
        }
        return row;
    }

}
