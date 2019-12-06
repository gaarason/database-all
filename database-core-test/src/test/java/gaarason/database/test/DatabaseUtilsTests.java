package gaarason.database.test;

import gaarason.database.utils.ExceptionUtil;
import gaarason.database.utils.FormatUtil;
import gaarason.database.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
public class DatabaseUtilsTests {

    @Test
    public void testEntityUtil(){

    }

    @Test
    public void testExceptionUtil(){
        Throwable e = new RuntimeException("message deadlock detected");
        Assert.assertTrue(ExceptionUtil.causedByDeadlock(e));
        Throwable e2 = new RuntimeException("message dea1dlock detected");
        Assert.assertFalse(ExceptionUtil.causedByDeadlock(e2));

    }

    @Test
    public void testFormatUtil(){
        // 给字段加上反引号
        Assert.assertEquals(FormatUtil.column(" sum(order.amount) AS sum_price  "), "sum(`order`.`amount`) as " +
            "`sum_price`");

        // 给字段加上单引号
        Assert.assertEquals(FormatUtil.quotes(" alice  "), "'alice'");

        // 字段格式化
        List<String> testList = new ArrayList<>();
        testList.add("name");
        testList.add("age");
        testList.add("sex");
        Assert.assertEquals(FormatUtil.column(testList), "`name`,`age`,`sex`");

        // 值加上括号
        Assert.assertEquals(FormatUtil.bracket("1765595948"), "(1765595948)");

        // 给与sql片段两端空格
        Assert.assertEquals(FormatUtil.spaces("abd"), " abd ");
    }

    @Test
    public void testStringUtil(){
        // 下划线转驼峰
        Assert.assertEquals(StringUtil.lineToHump("t_invoice"), "tInvoice");
        Assert.assertEquals(StringUtil.lineToHump("t_invoice", true), "TInvoice");
        Assert.assertEquals(StringUtil.lineToHump("_t_invoice", true), "TInvoice");
        Assert.assertEquals(StringUtil.lineToHump("_t_invoice"), "tInvoice");

        // 小驼峰转下划线
        Assert.assertEquals(StringUtil.humpToLine("tInvoice"), "t_invoice");
        Assert.assertEquals(StringUtil.humpToLine("invoice"), "invoice");
        Assert.assertEquals(StringUtil.humpToLine("_tInvoice"), "t_invoice");

        // 移除字符串左侧的所有character
        Assert.assertEquals(StringUtil.ltrim("tInvoice", "t"), "Invoice");
        Assert.assertEquals(StringUtil.ltrim("tInvoiceR", "tInvoice"), "R");
        Assert.assertEquals(StringUtil.ltrim("tTTtTTInvoice", "tTT"), "Invoice");
        Assert.assertEquals(StringUtil.ltrim("####Invoice", "##"), "Invoice");
        Assert.assertEquals(StringUtil.ltrim("#####Invoice", "##"), "#Invoice");

        // 移除字符串右侧的所有character
        Assert.assertEquals(StringUtil.rtrim("tInvoice@@", "@"), "tInvoice");
        Assert.assertEquals(StringUtil.rtrim("tInvoice\n\n", "\n"), "tInvoice");
        Assert.assertEquals(StringUtil.rtrim("tInvoice", "Invoice"), "t");
    }
}
