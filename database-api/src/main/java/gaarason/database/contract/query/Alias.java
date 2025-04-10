package gaarason.database.contract.query;

import java.io.Serializable;

public class Alias implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前表
     */
    protected String table = "";

    /**
     * 当前表, 别名
     */
    protected String alias = "";

    /**
     * 当前表, 别名占位符
     */
    protected String aliasPlaceHolder = "";

    public Alias() {
    }

    public Alias(String table, String alias) {
        this.table = table;
        this.aliasPlaceHolder = this.alias = alias;
    }

    public String getAliasPlaceHolder() {
        return aliasPlaceHolder;
    }

    public Alias setAliasPlaceHolder(String aliasPlaceHolder) {
        this.aliasPlaceHolder = aliasPlaceHolder;
        return this;
    }

    public String getAlias() {
        return alias;
    }

    public Alias setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public String getTable() {
        return table;
    }

    public Alias setTable(String table) {
        this.table = table;
        return this;
    }

    @Override
    public String toString() {
        return aliasPlaceHolder;
    }

}
