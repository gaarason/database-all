package gaarason.database.util;

import gaarason.database.contract.query.Grammar;

import java.util.Collection;
import java.util.List;

public class FormatUtil {

    /**
     * 给字段加上反引号
     * @param something 字段 eg: sum(order.amount) AS sum_price
     * @return eg: sum(`order`.`amount`) AS `sum_price`
     */
    public static String column(String something) {
        return backQuote(something);
    }

    /**
     * 给字段加上单引号
     * @param something 别名 eg: alice
     * @return eg: 'alice'
     */
    public static String quotes(String something) {
        return '\'' + something.trim() + '\'';
    }

    /**
     * 字段格式化
     * @param somethingList eg:[name,age,sex]
     * @return eg: `name`,`age`,`sex`
     */
    public static String column(List<String> somethingList) {
        StringBuilder StringBuilder = new StringBuilder();
        for (String value : somethingList) {
            StringBuilder.append(FormatUtil.column(value)).append(',');
        }
        return StringUtil.rtrim(StringBuilder.toString(), ",");
    }

    /**
     * 值转化为参数绑定?
     * @param something 字段 eg:小明
     * @return eg:?
     */
    public static String value(String something, Grammar grammar) {
        grammar.pushWhereParameter(something);
        return " ? ";
    }

    /**
     * 值转化为参数绑定?
     * @param something 字段 eg:小明
     * @return eg:?
     */
    public static String data(String something, Grammar grammar) {
        grammar.pushDataParameter(something);
        return " ? ";
    }

    /**
     * 值转化为参数绑定?
     * @param somethingList eg:[1,2,3]
     * @return eg: ? , ? , ?
     */
    public static String value(Collection<?> somethingList, Grammar grammar) {
        StringBuilder StringBuilder = new StringBuilder();
        for (Object value : somethingList) {
            StringBuilder.append(FormatUtil.value(String.valueOf(value), grammar)).append(',');
        }
        return StringUtil.rtrim(StringBuilder.toString(), ",");
    }

    /**
     * 值加上括号
     * @param something 字段 eg:1765595948
     * @return eg:(1765595948)
     */
    public static String bracket(String something) {
        return '(' + something + ')';
    }

    /**
     * 给与sql片段两端空格
     * @param something 字段 eg:abd
     * @return eg: abd
     */
    public static String spaces(String something) {
        return ' ' + something.trim() + ' ';
    }

    /**
     * 给字段加上反引号
     * @param something 字段 eg: sum(order.amount) AS sum_price
     * @return eg: sum(`order`.`amount`) AS `sum_price`
     */
    public static String backQuote(String something) {
        something = something.trim();
        int    whereIsAs    = something.toLowerCase().indexOf(" as ");
        String temp         = "";
        String mayBeHasFunc = something;
        String alias        = "";
        if (whereIsAs != -1) {
            mayBeHasFunc = something.substring(0, whereIsAs); // eg: sum(order.amount)
            alias = " as `" + something.substring(whereIsAs + 4) + "`";
        }
        int whereIsQuote = mayBeHasFunc.indexOf('(');
        if (whereIsQuote != -1) {
            String func     = mayBeHasFunc.substring(0, whereIsQuote); // eg: sum
            String someElse = mayBeHasFunc.replace(func, "").replace("(", "").replace(")", ""); // eg: order.amount

            int whereIsPoint = someElse.indexOf('.');
            if (whereIsPoint != -1) {
                String table  = someElse.substring(0, whereIsPoint); // eg: order
                String column = someElse.replace(table + '.', ""); // eg: amount
                temp = column.equals("*") ? '`' + table + "`." + column : '`' + table + "`.`" + column + '`';
            } else if ("".equals(someElse)) {
                temp = "";
            } else {
                temp = '`' + someElse + '`';
            }
            temp = func + '(' + temp + ')';
        } else {
            int whereIsPoint = mayBeHasFunc.indexOf('.');
            if (whereIsPoint == -1) {
                temp = '`' + mayBeHasFunc + '`';
            } else {
                String table  = mayBeHasFunc.substring(0, whereIsPoint); // eg: order
                String column = mayBeHasFunc.replace(table + '.', ""); // eg: amount
                temp = column.equals("*") ? '`' + table + "`." + column : '`' + table + "`.`" + column + '`';
            }
        }
        return temp + alias;
    }
}
