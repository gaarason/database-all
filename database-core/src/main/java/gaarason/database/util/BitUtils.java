package gaarason.database.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 位操作
 */
public final class BitUtils {

    private BitUtils() {
    }

    /**
     * 打包选项 ( 可以理解成, 选项转十进制数字 )
     * @param options 选项 eg: 0,1,2,3,4
     * @return 十进制数字
     */
    public static long pack(Collection<Object> options) {
        return setOptions(0, options);
    }

    /**
     * 打包选项 ( 可以理解成, 选项转十进制数字 )
     * @param option 选项 eg: 0,1,2,3,4
     * @return 十进制数字
     */
    public static long pack(Object option) {
        long l = Long.parseLong(String.valueOf(option));
        return setOption(0, l);
    }

    /**
     * 打包选项 ( 可以理解成, 选项转十进制数字 )
     * @param options 选项 eg: 0,1,2,3,4
     * @return 十进制数字
     */
    public static long pack(Object... options) {
        List<Object> collect = Arrays.stream(options).collect(Collectors.toList());
        return setOptions(0, collect);
    }

    /**
     * 解包成选项
     * @param packValue 打包后的十进制数字 eg: 0
     * @return 选项列表
     */
    public static List<Long> unpack(long packValue) {
        List<Long> selectedOptions = new ArrayList<>();

        // 遍历所有可能的选项位
        for (long i = 0; i < 32; i++) {
            long optionMask = 1 << i;
            if ((packValue & optionMask) != 0) {
                selectedOptions.add(i);
            }
        }

        return selectedOptions;
    }

    /**
     * 设置选项
     * @param packValue 打包后的十进制数字 eg: 0
     * @param options 选项值 eg: 0,1,2,3,4
     * @return 新值
     */
    public static long setOptions(Object packValue, Object... options) {
        List<Object> collect = Arrays.stream(options).collect(Collectors.toList());
        return setOptions(packValue, collect);
    }

    /**
     * 设置选项
     * @param packValue 打包后的十进制数字 eg: 0
     * @param options 选项值 eg: 0,1,2,3,4
     * @return 新值
     */
    public static long setOptions(Object packValue, Collection<Object> options) {
        long packValueLong = Long.parseLong(String.valueOf(packValue));
        List<Long> longList = new ArrayList<>();
        for (Object option : options) {
            longList.add(Long.parseLong(String.valueOf(option)));
        }
        return setOptions(packValueLong, longList);
    }

    /**
     * 设置选项
     * @param packValue 打包后的十进制数字 eg: 0
     * @param options 选项值 eg: 0,1,2,3,4
     * @return 新值
     */
    public static long setOptions(long packValue, Collection<Long> options) {
        for (long option : options) {
            packValue = setOption(packValue, option);
        }
        return packValue;
    }

    /**
     * 设置选项
     * @param packValue 打包后的十进制数字 eg: 0
     * @param option 选项值 eg: 0,1,2,3,4
     * @return 新值
     */
    public static long setOption(long packValue, long option) {
        long bitValue = 1L << option;
        packValue |= bitValue;
        return packValue;
    }

    /**
     * 取消选项
     * @param packValue 打包后的十进制数字 eg: 0
     * @param options 选项值 eg: 0,1,2,3,4
     * @return 新值
     */
    public static long unsetOptions(Object packValue, Object... options) {
        List<Object> collect = Arrays.stream(options).collect(Collectors.toList());
        return unsetOptions(packValue, collect);
    }


    /**
     * 取消选项
     * @param packValue 打包后的十进制数字 eg: 0
     * @param options 选项值 eg: 0,1,2,3,4
     * @return 新值
     */
    public static long unsetOptions(Object packValue, Collection<Object> options) {
        long packValueLong = Long.parseLong(String.valueOf(packValue));
        List<Long> longList = new ArrayList<>();
        for (Object option : options) {
            longList.add(Long.parseLong(String.valueOf(option)));
        }
        return unsetOptions(packValueLong, longList);
    }

    /**
     * 取消选项
     * @param packValue 打包后的十进制数字 eg: 0
     * @param options 选项值 eg: 0,1,2,3,4
     * @return 新值
     */
    public static long unsetOptions(long packValue, Collection<Long> options) {
        for (long value : options) {
            packValue = unsetOption(packValue, value);
        }
        return packValue;
    }

    /**
     * 取消选项
     * @param packValue 打包后的十进制数字 eg: 0
     * @param option 选项值 eg: 0,1,2,3,4
     * @return 新值
     */
    public static long unsetOption(long packValue, long option) {
        long bitValue = 1L << option;
        packValue &= ~bitValue;
        return packValue;
    }

    /**
     * 是否已设置选项
     * @param packValue 打包后的十进制数字 eg: 0
     * @param option 选项值 eg: 0,1,2,3,4
     * @return boolean
     */
    public static boolean checkOptionSet(long packValue, long option) {
        long bitValue = 1L << option;
        return (packValue & bitValue) != 0;
    }

}