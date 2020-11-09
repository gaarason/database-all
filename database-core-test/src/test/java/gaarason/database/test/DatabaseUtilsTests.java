package gaarason.database.test;

import gaarason.database.contract.eloquent.Model;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.test.models.relation.pojo.Teacher;
import gaarason.database.test.utils.MultiThreadUtil;
import gaarason.database.util.ExceptionUtil;
import gaarason.database.util.FormatUtil;
import gaarason.database.util.ObjectUtil;
import gaarason.database.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.reflections8.Reflections;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
public class DatabaseUtilsTests {

    @Test
    public void testEntityUtil() {

    }

    @Test
    public void testExceptionUtil() {
        Throwable e = new RuntimeException("message deadlock detected");
        Assert.assertTrue(ExceptionUtil.causedByDeadlock(e));
        Throwable e2 = new RuntimeException("message dea1dlock detected");
        Assert.assertFalse(ExceptionUtil.causedByDeadlock(e2));

    }

    @Test
    public void testFormatUtil() {
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
    public void testStringUtil() {
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

    @Test
    public void testSnowFlakeIdUtil() throws InterruptedException {
        ArrayList<Long> ids = new ArrayList<>();

        int a = 100;
        int b = 30000;

        MultiThreadUtil.run(a, b, () -> {
            long id = ModelShadowProvider.getIdGenerators().getSnowFlakesID().nextId();
            synchronized (ids) {
                ids.add(id);
            }
        });
        System.out.println("生成id数量: " + ids.size());
        Assert.assertEquals(a * b, ids.size());

        // 去重
        LinkedHashSet<Long> hashSet               = new LinkedHashSet<>(ids);
        ArrayList<Long>     listWithoutDuplicates = new ArrayList<>(hashSet);
        Assert.assertEquals("存在重复的id", ids.size(), listWithoutDuplicates.size());
        System.out.println("没有重复id");
    }

    @Test
    public void testCheckProperties() {
        boolean student = ObjectUtil.checkProperties(Teacher.class, "students");
        Assert.assertTrue(student);

        boolean student11 = ObjectUtil.checkProperties(Teacher.class, "students11");
        Assert.assertFalse(student11);

        boolean o = ObjectUtil.checkProperties(Teacher.class, "student");
        Assert.assertTrue(o);

        boolean o1 = ObjectUtil.checkProperties(Teacher.class, "student.teacherId");
        Assert.assertTrue(o1);

        boolean o11 = ObjectUtil.checkProperties(Teacher.class, "student.teacherIds");
        Assert.assertFalse(o11);

        boolean o2 = ObjectUtil.checkProperties(Teacher.class, "student.teacher.age");
        Assert.assertTrue(o2);

        boolean o3 = ObjectUtil.checkProperties(Teacher.class, "student.teacher.age2");
        Assert.assertFalse(o3);

        boolean o4 = ObjectUtil.checkProperties(Teacher.class, "student.teacher.students.teacher.students" +
            ".teacher.id");
        Assert.assertTrue(o4);
    }

    @Test
    public void test() {
        Reflections                 reflections = new Reflections("lombok","gaarason.database","");
        Set<Class<? extends Model>> subTypesOf  = reflections.getSubTypesOf(Model.class);
        System.out.println(subTypesOf);

//        Reflections                 reflections1 = new Reflections("lombok");
//        Set<Class<? extends Model>> subTypesOf1  = reflections1.getSubTypesOf(Model.class);
//        System.out.println(subTypesOf1);


    }

}
