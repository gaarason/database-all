package gaarason.database.test;

import gaarason.database.appointment.LambdaInfo;
import gaarason.database.bootstrap.ContainerBootstrap;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.support.IdGenerator;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.test.models.relation.pojo.Student;
import gaarason.database.test.models.relation.pojo.Teacher;
import gaarason.database.test.utils.MultiThreadUtil;
import gaarason.database.util.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.reflections.Reflections;

import java.util.*;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
public class DatabaseUtilsTests {

    @Test
    public void testEntityUtil() {

    }

    @Test
    public void testExceptionUtil() {
        Throwable e = new RuntimeException("message deadlock detected");
        Assert.assertTrue(ExceptionUtils.causedByDeadlock(e));
        Throwable e2 = new RuntimeException("message dea1dlock detected");
        Assert.assertFalse(ExceptionUtils.causedByDeadlock(e2));

    }

    @Test
    public void testFormatUtil() {
        // 给字段加上反引号
        Assert.assertEquals(FormatUtils.column(" sum(order.amount) AS sum_price  "), "sum(`order`.`amount`) as " +
            "`sum_price`");

        // 给字段加上单引号
        Assert.assertEquals(FormatUtils.quotes(" alice  "), "'alice'");

        // 字段格式化
        List<String> testList = new ArrayList<>();
        testList.add("name");
        testList.add("age");
        testList.add("sex");
        Assert.assertEquals(FormatUtils.column(testList), "`name`,`age`,`sex`");

        // 值加上括号
        Assert.assertEquals(FormatUtils.bracket("1765595948"), "(1765595948)");

        // 给与sql片段两端空格
        Assert.assertEquals(FormatUtils.spaces("abd"), " abd ");
    }

    @Test
    public void testStringUtil() {
        // 下划线转驼峰
        Assert.assertEquals(StringUtils.lineToHump("t_invoice"), "tInvoice");
        Assert.assertEquals(StringUtils.lineToHump("t_invoice", true), "TInvoice");
        Assert.assertEquals(StringUtils.lineToHump("_t_invoice", true), "TInvoice");
        Assert.assertEquals(StringUtils.lineToHump("_t_invoice"), "tInvoice");

        // 小驼峰转下划线
        Assert.assertEquals(StringUtils.humpToLine("tInvoice"), "t_invoice");
        Assert.assertEquals(StringUtils.humpToLine("invoice"), "invoice");
        Assert.assertEquals(StringUtils.humpToLine("_tInvoice"), "t_invoice");

        // 移除字符串左侧的所有character
        Assert.assertEquals(StringUtils.ltrim("tInvoice", "t"), "Invoice");
        Assert.assertEquals(StringUtils.ltrim("tInvoiceR", "tInvoice"), "R");
        Assert.assertEquals(StringUtils.ltrim("tTTtTTInvoice", "tTT"), "Invoice");
        Assert.assertEquals(StringUtils.ltrim("####Invoice", "##"), "Invoice");
        Assert.assertEquals(StringUtils.ltrim("#####Invoice", "##"), "#Invoice");

        // 移除字符串右侧的所有character
        Assert.assertEquals(StringUtils.rtrim("tInvoice@@", "@"), "tInvoice");
        Assert.assertEquals(StringUtils.rtrim("tInvoice\n\n", "\n"), "tInvoice");
        Assert.assertEquals(StringUtils.rtrim("tInvoice", "Invoice"), "t");
    }

    @Test
    public void testSnowFlakeIdUtil() throws InterruptedException {
        List<Long> ids = new LinkedList<>();

        int a = 100;
        int b = 30000;
        IdGenerator.SnowFlakesID snowFlakesID = ContainerBootstrap.buildAndBootstrap().getBean(IdGenerator.SnowFlakesID.class);
        MultiThreadUtil.run(a, b, () -> {
            long id = snowFlakesID.nextId();
            synchronized (ids) {
                ids.add(id);
            }
        });
        System.out.println("生成id数量: " + ids.size());
        Assert.assertEquals(a * b, ids.size());

        // 去重
        LinkedHashSet<Long> hashSet = new LinkedHashSet<>(ids);
        ArrayList<Long> listWithoutDuplicates = new ArrayList<>(hashSet);
        Assert.assertEquals("存在重复的id", ids.size(), listWithoutDuplicates.size());
        System.out.println("没有重复id");

        System.out.println(Long.MAX_VALUE);
        System.out.println(snowFlakesID.nextId());
        System.out.println(snowFlakesID.nextId());
        System.out.println(snowFlakesID.nextId());
        System.out.println(snowFlakesID.nextId());
    }

    @Test
    public void testCheckProperties() {
        boolean student = ObjectUtils.checkProperties(Teacher.class, "students");
        Assert.assertTrue(student);

        boolean student11 = ObjectUtils.checkProperties(Teacher.class, "students11");
        Assert.assertFalse(student11);

        boolean o = ObjectUtils.checkProperties(Teacher.class, "student");
        Assert.assertTrue(o);

        boolean o1 = ObjectUtils.checkProperties(Teacher.class, "student.teacherId");
        Assert.assertTrue(o1);

        boolean o11 = ObjectUtils.checkProperties(Teacher.class, "student.teacherIds");
        Assert.assertFalse(o11);

        boolean o2 = ObjectUtils.checkProperties(Teacher.class, "student.teacher.age");
        Assert.assertTrue(o2);

        boolean o3 = ObjectUtils.checkProperties(Teacher.class, "student.teacher.age2");
        Assert.assertFalse(o3);

        boolean o4 = ObjectUtils.checkProperties(Teacher.class, "student.teacher.students.teacher.students" +
            ".teacher.id");
        Assert.assertTrue(o4);
    }

    @Test
    public void test() {

        Reflections reflections = new Reflections();
//        Reflections reflections = new Reflections("lombok", "gaarason.database", "*");
        Set<Class<? extends Model>> subTypesOf = reflections.getSubTypesOf(Model.class);
        System.out.println(subTypesOf);

//        Reflections                 reflections1 = new Reflections("lombok");
//        Set<Class<? extends Model>> subTypesOf1  = reflections1.getSubTypesOf(Model.class);
//        System.out.println(subTypesOf1);

    }

    @Test
    public void random() {
        final Set<Integer> random1 = ObjectUtils.random(10000, 1000);
        Assert.assertEquals(1000, random1.size());
        System.out.println(random1);

        final Set<Integer> random2 = ObjectUtils.random(10000, 9000);
        Assert.assertEquals(9000, random2.size());

        final Set<Integer> random3 = ObjectUtils.random(1, 1);
        Assert.assertEquals(1, random3.size());

        final Set<Integer> random4 = ObjectUtils.random(0, 0);
        Assert.assertEquals(0, random4.size());

        final Set<Integer> random5 = ObjectUtils.random(10000, 10000);
        Assert.assertEquals(10000, random5.size());
        System.out.println(random5);

        Set<Integer> e = new HashSet<>();
        e.add(2);
        e.add(1);
        e.add(0);
        System.out.println(e);

    }

    @Test
    public void getColumnByMethodTest() {
        MultiThreadUtil.run(100, 1000, () -> {
            LambdaInfo<Student> name = LambdaUtils.parse(Student::getName);
            LambdaUtils.parse(Student::getName);
            LambdaUtils.parse(Student::getName);
            LambdaUtils.parse(Student::getName);
            LambdaUtils.parse(Student::getName);
            LambdaUtils.parse(Student::getName);
            LambdaUtils.parse(Student::getName);
            LambdaUtils.parse(Student::getName);
            LambdaUtils.parse(Student::getName);
            LambdaUtils.parse(Student::getName);
            LambdaUtils.parse(Student::getName);
            LambdaUtils.parse(Student::getName);
            LambdaUtils.parse(Student::getName);
            LambdaUtils.parse(Student::getName);
            LambdaUtils.parse(Student::getName);
            LambdaUtils.parse(Student::getName);
            LambdaUtils.parse(Student::getName);
            LambdaUtils.parse(Student::getName);
            LambdaUtils.parse(Student::getName);
            LambdaUtils.parse(Student::getName);
            LambdaUtils.parse(Student::getName);
            LambdaUtils.parse(Student::getName);
            LambdaUtils.parse(Student::getName);
            LambdaUtils.parse(Student::getName);
            LambdaUtils.parse(Student::getName);
            LambdaUtils.parse(Student::getName);
            LambdaUtils.parse(Student::getName);
            LambdaUtils.parse(Student::getName);
            LambdaInfo<Student> sex = LambdaUtils.parse(Student::getSex);
            Assert.assertEquals("name", name.getFieldName());
            Assert.assertEquals("sex", sex.getFieldName());
        });
        ModelShadowProvider modelShadowProvider = ContainerBootstrap.build().bootstrap().getBean(ModelShadowProvider.class);
        modelShadowProvider.parseColumnNameByLambdaWithCache(Student::getName);
        MultiThreadUtil.run(100, 1000, () -> {
            String column = modelShadowProvider.parseColumnNameByLambdaWithCache(Student::getName);
            modelShadowProvider.parseColumnNameByLambdaWithCache(Student::getName);
            modelShadowProvider.parseColumnNameByLambdaWithCache(Student::getName);
            modelShadowProvider.parseColumnNameByLambdaWithCache(Student::getName);
            modelShadowProvider.parseColumnNameByLambdaWithCache(Student::getName);
            modelShadowProvider.parseColumnNameByLambdaWithCache(Student::getName);
            modelShadowProvider.parseColumnNameByLambdaWithCache(Student::getName);
            modelShadowProvider.parseColumnNameByLambdaWithCache(Student::getName);
            modelShadowProvider.parseColumnNameByLambdaWithCache(Student::getName);
            modelShadowProvider.parseColumnNameByLambdaWithCache(Student::getName);
            modelShadowProvider.parseColumnNameByLambdaWithCache(Student::getName);
            modelShadowProvider.parseColumnNameByLambdaWithCache(Student::getName);
            modelShadowProvider.parseColumnNameByLambdaWithCache(Student::getName);
            modelShadowProvider.parseColumnNameByLambdaWithCache(Student::getName);
            modelShadowProvider.parseColumnNameByLambdaWithCache(Student::getName);
            modelShadowProvider.parseColumnNameByLambdaWithCache(Student::getName);
            modelShadowProvider.parseColumnNameByLambdaWithCache(Student::getName);
            modelShadowProvider.parseColumnNameByLambdaWithCache(Student::getName);
            modelShadowProvider.parseColumnNameByLambdaWithCache(Student::getName);
            modelShadowProvider.parseColumnNameByLambdaWithCache(Student::getName);
            modelShadowProvider.parseColumnNameByLambdaWithCache(Student::getName);
            modelShadowProvider.parseColumnNameByLambdaWithCache(Student::getName);
            modelShadowProvider.parseColumnNameByLambdaWithCache(Student::getName);
            modelShadowProvider.parseColumnNameByLambdaWithCache(Student::getName);
            modelShadowProvider.parseColumnNameByLambdaWithCache(Student::getName);
            modelShadowProvider.parseColumnNameByLambdaWithCache(Student::getName);
            modelShadowProvider.parseColumnNameByLambdaWithCache(Student::getName);
            modelShadowProvider.parseColumnNameByLambdaWithCache(Student::getName);
            String Sex = modelShadowProvider.parseColumnNameByLambdaWithCache(Student::getSex);
            Assert.assertEquals("name", column);
            Assert.assertEquals("sex", Sex);
        });
    }

}
