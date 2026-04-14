package gaarason.database.util;

import gaarason.database.lang.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 位操作
 */
public final class BitUtils {

    private BitUtils() {
    }

    /**
     * 打包选项 ( 可以理解成, 选项转十进制数字 )
     * @param options 选项 eg: 0,1,2,3,4 ... 63
     * @return 十进制数字
     */
    public static long packs(@Nullable Collection<?> options) {
        return setOptions(0, options);
    }

    /**
     * 打包选项 ( 可以理解成, 选项转十进制数字 )
     * @param option 选项 eg: 0,1,2,3,4 ... 63
     * @return 十进制数字
     */
    public static long pack(@Nullable Object option) {
        if (option == null) {
            return 0;
        }
        int l = Integer.parseInt(String.valueOf(option));
        return setOption(0, l);
    }

    /**
     * 打包选项 ( 可以理解成, 选项转十进制数字 )
     * @param options 选项 eg: 0,1,2,3,4 ... 63
     * @return 十进制数字
     */
    public static long packArr(Object... options) {
        List<Object> collect = Arrays.stream(options).collect(Collectors.toList());
        return setOptions(0, collect);
    }

    /**
     * 解包成选项
     * @param packValue 打包后的十进制数字 eg: 0
     * @return 选项列表
     */
    public static List<Integer> unpack(@Nullable Object packValue) {
        if (packValue == null) {
            return Collections.emptyList();
        }
        long packValueLong = Long.parseLong(String.valueOf(packValue));
        return unpack(packValueLong);
    }

    /**
     * 解包成选项
     * @param packValue 打包后的十进制数字 eg: 0
     * @return 选项列表
     */
    public static List<Integer> unpack(long packValue) {
        List<Integer> res = new ArrayList<>(Long.bitCount(packValue));
        long x = packValue;
        while (x != 0) {
            // 找到当前最低位的 1
            int bit = Long.numberOfTrailingZeros(x);

            // 加入结果
            res.add(bit);

            // 清掉这一位
            x &= (x - 1);
        }
        return res;
    }

    /**
     * 设置选项
     * @param packValue 打包后的十进制数字 eg: 0
     * @param options 选项值 eg: 0,1,2,3,4 ... 63
     * @return 新值
     */
    public static long setOptions(long packValue, Integer... options) {
        List<Integer> collect = Arrays.stream(options).collect(Collectors.toList());
        return setOptions(packValue, collect);
    }

    /**
     * 设置选项
     * @param packValue 打包后的十进制数字 eg: 0
     * @param options 选项值 eg: 0,1,2,3,4 ... 63
     * @return 新值
     */
    public static long setOptions(Object packValue, @Nullable Collection<?> options) {
        long packValueLong = Long.parseLong(String.valueOf(packValue));
        if (options == null) {
            return packValueLong;
        }
        List<Integer> longList = new ArrayList<>();
        for (Object option : options) {
            longList.add(Integer.parseInt(String.valueOf(option)));
        }
        return setOptions(packValueLong, longList);
    }

    /**
     * 设置选项
     * @param packValue 打包后的十进制数字 eg: 0
     * @param options 选项值 eg: 0,1,2,3,4 ... 63
     * @return 新值
     */
    public static long setOptions(long packValue, Collection<Integer> options) {
        for (int option : options) {
            packValue = setOption(packValue, option);
        }
        return packValue;
    }

    /**
     * 设置选项
     * @param packValue 打包后的十进制数字 eg: 0
     * @param option 选项值 eg: 0,1,2,3,4 ... 63
     * @return 新值
     */
    public static long setOption(long packValue, int option) {
        long bitValue = 1L << option;
        packValue |= bitValue;
        return packValue;
    }

    /**
     * 取消选项
     * @param packValue 打包后的十进制数字 eg: 0
     * @param options 选项值 eg: 0,1,2,3,4 ... 63
     * @return 新值
     */
    public static long unsetOptions(Object packValue, Object... options) {
        List<Object> collect = Arrays.stream(options).collect(Collectors.toList());
        return unsetOptions(packValue, collect);
    }


    /**
     * 取消选项
     * @param packValue 打包后的十进制数字 eg: 0
     * @param options 选项值 eg: 0,1,2,3,4 ... 63
     * @return 新值
     */
    public static long unsetOptions(Object packValue, Collection<Object> options) {
        long packValueLong = Long.parseLong(String.valueOf(packValue));
        List<Integer> intList = new ArrayList<>();
        for (Object option : options) {
            intList.add(Integer.parseInt(String.valueOf(option)));
        }
        return unsetOptions(packValueLong, intList);
    }

    /**
     * 取消选项
     * @param packValue 打包后的十进制数字 eg: 0
     * @param options 选项值 eg: 0,1,2,3,4 ... 63
     * @return 新值
     */
    public static long unsetOptions(long packValue, Collection<Integer> options) {
        for (int value : options) {
            packValue = unsetOption(packValue, value);
        }
        return packValue;
    }

    /**
     * 取消选项
     * @param packValue 打包后的十进制数字 eg: 0
     * @param option 选项值 eg: 0,1,2,3,4 ... 63
     * @return 新值
     */
    public static long unsetOption(long packValue, int option) {
        long bitValue = 1L << option;
        packValue &= ~bitValue;
        return packValue;
    }

    /**
     * 是否已设置选项
     * @param packValue 打包后的十进制数字 eg: 0
     * @param option 选项值 eg: 0,1,2,3,4 ... 63
     * @return boolean
     */
    public static boolean checkOptionSet(long packValue, int option) {
        long bitValue = 1L << option;
        return (packValue & bitValue) != 0;
    }

}