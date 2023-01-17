package gaarason.database.util;

import java.util.List;
import java.util.Locale;

/**
 * 格式化
 * @author xt
 */
public class FormatUtils {

    private FormatUtils() {
    }

    /**
     * 给字段加上反引号
     * @param something 字段 eg: sum(order.amount) AS sum_price
     * @return eg: sum(`order`.`amount`) AS `sum_price`
     */
    public static String column(String something) {
        return backQuote(something, "`");
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
        StringBuilder stringBuilder = new StringBuilder();
        for (String value : somethingList) {
            stringBuilder.append(FormatUtils.column(value)).append(',');
        }
        return StringUtils.rtrim(stringBuilder.toString(), ",");
    }

    /**
     * 值加上括号
     * @param something 字段 eg:1765595948
     * @return eg:(1765595948)
     */
    public static String bracket(String something) {
        StringBuilder stringBuilder = new StringBuilder(something);
        bracket(stringBuilder);
        return stringBuilder.toString();
    }

    /**
     * 值加上括号
     * @param stringBuilder 字段 eg:1765595948 -> (1765595948)
     */
    public static void bracket(StringBuilder stringBuilder) {
        stringBuilder.insert(0, '(').append(')');
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
     * @param symbol 符号 eg: `
     * @return eg: sum(`order`.`amount`) AS `sum_price`
     */
    public static String backQuote(String something, String symbol) {
        something = something.trim();
        int whereIsAs = something.toLowerCase(Locale.ENGLISH).indexOf(" as ");
        String temp;
        String mayBeHasFunc = something;
        String alias = "";
        if (whereIsAs != -1) {
            mayBeHasFunc = something.substring(0, whereIsAs); // eg: sum(order.amount)
            alias = " as " + symbol + something.substring(whereIsAs + 4) + symbol;
        }
        int whereIsQuote = mayBeHasFunc.indexOf('(');
        if (whereIsQuote != -1) {
            String func = mayBeHasFunc.substring(0, whereIsQuote); // eg: sum

            String someElse = StringUtils.replace(
                StringUtils.replace(StringUtils.replace(mayBeHasFunc, func, ""), "(", ""), ")", ""); // eg: order.amount

            int whereIsPoint = someElse.indexOf('.');
            if (whereIsPoint != -1) {
                String table = someElse.substring(0, whereIsPoint); // eg: order
                String column = someElse.replace(table + '.', ""); // eg: amount
                temp = column.equals("*") ? symbol + table + symbol + "." + column :
                    symbol + table + symbol + "." + symbol + column + symbol;
            } else if ("".equals(someElse)) {
                temp = "";
            } else {
                temp = symbol + someElse + symbol;
            }
            temp = func + '(' + temp + ')';
        } else {
            int whereIsPoint = mayBeHasFunc.indexOf('.');
            if (whereIsPoint == -1) {
                temp = symbol + mayBeHasFunc + symbol;
            } else {
                String table = mayBeHasFunc.substring(0, whereIsPoint); // eg: order
                String column = mayBeHasFunc.replace(table + '.', ""); // eg: amount
                temp = column.equals("*") ? symbol + table + symbol + "." + column :
                    symbol + table + symbol + "." + symbol + column + symbol;
            }
        }
        return temp + alias;
    }
}
