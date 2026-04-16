package gaarason.database.appointment;

import gaarason.database.lang.Nullable;

import java.util.Locale;

/**
 * 数据库类型枚举
 * <p>
 * 每个枚举值包含: JDBC 产品名匹配关键字、描述、标识符引号、方言分组
 * @author xt
 */
public enum DbType {

    MYSQL("mysql", "MySQL 数据库", "`", DialectGroup.MYSQL),
    MARIADB("mariadb", "MariaDB 数据库", "`", DialectGroup.MYSQL),
    ORACLE("oracle", "Oracle11g 及以下数据库", "\"", DialectGroup.ORACLE),
    ORACLE_12C("oracle12c", "Oracle12c 及以上数据库", "\"", DialectGroup.ORACLE_12C),
    DB2("db2", "DB2 数据库", "\"", DialectGroup.DB2),
    H2("h2", "H2 数据库", "\"", DialectGroup.POSTGRESQL),
    HSQL("hsql", "HSQL 数据库", "\"", DialectGroup.POSTGRESQL),
    SQLITE("sqlite", "SQLite 数据库", "\"", DialectGroup.POSTGRESQL),
    POSTGRESQL("postgresql", "PostgreSQL 数据库", "\"", DialectGroup.POSTGRESQL),
    SQL_SERVER_2005("sqlserver2005", "SQLServer2005 数据库", "\"", DialectGroup.SQL_SERVER),
    SQL_SERVER("sqlserver", "SQLServer 数据库", "\"", DialectGroup.SQL_SERVER),
    DM("dm", "达梦数据库", "\"", DialectGroup.ORACLE_12C),
    XUGU("xugu", "虚谷数据库", "\"", DialectGroup.ORACLE_12C),
    KINGBASE_ES("kingbasees", "人大金仓数据库", "\"", DialectGroup.POSTGRESQL),
    PHOENIX("phoenix", "Phoenix HBase 数据库", "\"", DialectGroup.POSTGRESQL),
    GAUSS("gauss", "Gauss 数据库", "\"", DialectGroup.POSTGRESQL),
    CLICK_HOUSE("clickhouse", "ClickHouse 数据库", "`", DialectGroup.POSTGRESQL),
    GBASE("gbase", "南大通用(华库)数据库", "`", DialectGroup.MYSQL),
    GBASE_8S("gbase-8s", "南大通用数据库 GBase 8s", "\"", DialectGroup.INFORMIX),
    OSCAR("oscar", "神通数据库", "\"", DialectGroup.POSTGRESQL),
    SYBASE("sybase", "Sybase ASE 数据库", "\"", DialectGroup.SQL_SERVER),
    OCEAN_BASE("oceanbase", "OceanBase 数据库", "`", DialectGroup.MYSQL),
    FIREBIRD("firebird", "Firebird 数据库", "\"", DialectGroup.FIREBIRD),
    DERBY("derby", "Derby 数据库", "\"", DialectGroup.ORACLE_12C),
    HIGH_GO("highgo", "瀚高数据库", "\"", DialectGroup.POSTGRESQL),
    CUBRID("cubrid", "CUBRID 数据库", "`", DialectGroup.MYSQL),
    GOLDILOCKS("goldilocks", "GOLDILOCKS 数据库", "\"", DialectGroup.ORACLE_12C),
    CSIIDB("csiidb", "CSIIDB 数据库", "\"", DialectGroup.POSTGRESQL),
    SAP_HANA("hana", "SAP_HANA 数据库", "\"", DialectGroup.POSTGRESQL),
    IMPALA("impala", "Impala 数据库", "`", DialectGroup.POSTGRESQL),
    VERTICA("vertica", "Vertica 数据库", "\"", DialectGroup.POSTGRESQL),
    XCloud("xcloud", "行云数据库", "\"", DialectGroup.POSTGRESQL),
    REDSHIFT("redshift", "亚马逊 redshift 数据库", "\"", DialectGroup.POSTGRESQL),
    OPEN_GAUSS("opengauss", "华为 openGauss 数据库", "\"", DialectGroup.POSTGRESQL),
    TDENGINE("tdengine", "TDengine 数据库", "`", DialectGroup.MYSQL),
    INFORMIX("informix", "Informix 数据库", "\"", DialectGroup.INFORMIX),
    GREENPLUM("greenplum", "Greenplum 数据库", "\"", DialectGroup.POSTGRESQL),
    UXDB("uxdb", "优炫数据库", "\"", DialectGroup.POSTGRESQL),
    DORIS("doris", "Doris 数据库", "`", DialectGroup.MYSQL),
    HIVE("hive", "Hive 数据库", "`", DialectGroup.MYSQL),
    LEALONE("lealone", "Lealone 数据库", "\"", DialectGroup.POSTGRESQL),
    SINODB("sinodb", "星瑞格数据库", "\"", DialectGroup.INFORMIX),
    ;

    /**
     * JDBC getDatabaseProductName() 匹配关键字(小写)
     */
    private final String keyword;

    /**
     * 数据库描述
     */
    private final String desc;

    /**
     * 标识符引号字符(反引号 or 双引号)
     */
    private final String identifierQuote;

    /**
     * 方言分组, 决定 SQL 语法差异(分页、UPSERT 等)
     */
    private final DialectGroup dialectGroup;

    DbType(String keyword, String desc, String identifierQuote, DialectGroup dialectGroup) {
        this.keyword = keyword;
        this.desc = desc;
        this.identifierQuote = identifierQuote;
        this.dialectGroup = dialectGroup;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getDesc() {
        return desc;
    }

    public String getIdentifierQuote() {
        return identifierQuote;
    }

    public DialectGroup getDialectGroup() {
        return dialectGroup;
    }

    /**
     * 通过 JDBC DatabaseMetaData.getDatabaseProductName() 返回值检测数据库类型
     * @param productName JDBC 产品名(大小写不敏感)
     * @return 匹配的 DbType, 未匹配返回 null
     */
    @Nullable
    public static DbType fromProductName(String productName) {
        if (productName == null || productName.isEmpty()) {
            return null;
        }
        String lowerName = productName.toLowerCase(Locale.ENGLISH);

        // oracle12c 需要通过版本号判断, 这里仅通过 productName 无法区分
        // 优先精确匹配(按枚举定义顺序)
        for (DbType dbType : values()) {
            if (lowerName.contains(dbType.keyword)) {
                return dbType;
            }
        }
        return null;
    }

    /**
     * 方言分组
     * <p>
     * 同组数据库共享分页、UPSERT 等核心 SQL 语法
     */
    public enum DialectGroup {
        /**
         * MySQL 方言: LIMIT offset,count ; ON DUPLICATE KEY UPDATE
         */
        MYSQL,
        /**
         * PostgreSQL 方言: LIMIT count OFFSET offset ; ON CONFLICT DO UPDATE
         */
        POSTGRESQL,
        /**
         * Oracle 11g 方言: ROWNUM 包裹子查询
         */
        ORACLE,
        /**
         * Oracle 12c / DM / Derby 方言: OFFSET n ROWS FETCH NEXT m ROWS ONLY
         */
        ORACLE_12C,
        /**
         * SQL Server 方言: OFFSET n ROWS FETCH NEXT m ROWS ONLY
         */
        SQL_SERVER,
        /**
         * DB2 方言: FETCH FIRST n ROWS ONLY
         */
        DB2,
        /**
         * Informix 方言: SKIP m FIRST n (置于 SELECT 之后)
         */
        INFORMIX,
        /**
         * Firebird 方言: ROWS m TO n
         */
        FIREBIRD,
    }
}
