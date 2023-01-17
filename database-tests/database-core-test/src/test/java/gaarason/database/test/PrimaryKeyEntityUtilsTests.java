package gaarason.database.test;

import gaarason.database.annotation.Primary;
import gaarason.database.test.models.normal.NullTestModel;
import gaarason.database.util.EntityUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;

public class PrimaryKeyEntityUtilsTests {

    @Test
    public void getDeclaredFieldsContainParentTest() {
        List<Field> fields = EntityUtils.getDeclaredFieldsContainParent(Son.class);
        System.out.println(fields);

        /**
         * 使用测试覆盖率工具时, 会在类中在增加  类似于 private static transient int[] gaarason.database.test.EntityUtilsTests$Son.__$lineHits$__,
         [
         private static final long gaarason.database.test.EntityUtilsTests$Son.serialVersionUID,
         private java.lang.String gaarason.database.test.EntityUtilsTests$Son.name,
         private java.lang.Integer gaarason.database.test.EntityUtilsTests$Son.age,
         private java.lang.String gaarason.database.test.EntityUtilsTests$Son.onlySon,
         private static transient int[] gaarason.database.test.EntityUtilsTests$Son.__$lineHits$__,
         private static final long gaarason.database.test.EntityUtilsTests$Father.serialVersionUID,
         private java.lang.String gaarason.database.test.EntityUtilsTests$Father.name,
         private java.lang.Integer gaarason.database.test.EntityUtilsTests$Father.age,
         protected java.lang.Integer gaarason.database.test.EntityUtilsTests$Father.sex,
         private java.lang.String gaarason.database.test.EntityUtilsTests$Father.onlyFather,
         private static transient int[] gaarason.database.test.EntityUtilsTests$Father.__$lineHits$__
         ]
         */
        Assert.assertEquals(9, fields.size());
    }

    @Test
    public void 匿名类() {

        Son sonSon = new Son() {

            private static final long serialVersionUID = 1L;
            private String name = "son son";
            private Integer age = 6;
            private String onlySonSon = "onlySonSon";
            Son this$0 = new Son();
            Son this$0$;

            @Override
            public String getName() {
                return name;
            }

            @Override
            public void setName(String name) {
                this.name = name;
            }

            @Override
            public Integer getAge() {
                return age;
            }

            @Override
            public void setAge(Integer age) {
                this.age = age;
            }

            public String getOnlySonSon() {
                return onlySonSon;
            }

            public void setOnlySonSon(String onlySonSon) {
                this.onlySonSon = onlySonSon;
            }
        };

        List<Field> fields = EntityUtils.getDeclaredFieldsContainParent(sonSon.getClass());
        System.out.println(fields);
        Assert.assertEquals(13, fields.size());
    }

    @Test
    public void getDeclaredFieldsContainParentWithoutStaticTest() {
        List<Field> fields = EntityUtils.getDeclaredFieldsContainParentWithoutStatic(Son.class);
        Assert.assertEquals(7, fields.size());
    }

    @Test
    public void getDeclaredFieldContainParentTest() throws NoSuchFieldException, IllegalAccessException {
        // 本类特有属性
        Son son = new Son();
        String s = "onlySon 0";
        Field fieldOnlySon = EntityUtils.getDeclaredFieldContainParent(Son.class, "onlySon");
        Assert.assertNotNull(fieldOnlySon);
        fieldOnlySon.setAccessible(true);
        fieldOnlySon.set(son, s);
        Assert.assertEquals(son.getOnlySon(), s);

        // 本类 与 父类都  各自有的 private 属性
        Son son1 = new Son();
        Integer age1 = 99;
        // 会优先获取到子类
        Field fieldAge = EntityUtils.getDeclaredFieldContainParent(Son.class, "age");
        Assert.assertNotNull(fieldAge);
        fieldAge.setAccessible(true);
        fieldAge.set(son1, age1);
        Assert.assertEquals(son1.getAge(), age1);


        // 本类 与 父类都  各自有的 private 属性
        Son son11 = new Son();
        Integer age000 = son11.getAge();
        Integer age11 = 99;
        // 指定获取父类
        Field fieldAge1 = EntityUtils.getDeclaredFieldContainParent(Father.class, "age");
        Assert.assertNotNull(fieldAge1);
        fieldAge1.setAccessible(true);
        // 通过子类设置父类
        fieldAge1.set(son11, age11);
        // 子类的不变
        Assert.assertEquals(son11.getAge(), age000);

        // 仅 父类 有的 protected 属性
        Son son2 = new Son();
        Integer sex1 = 4;
        Field fieldSex = EntityUtils.getDeclaredFieldContainParent(Son.class, "sex");
        Assert.assertNotNull(fieldSex);
        fieldSex.setAccessible(true);
        fieldSex.set(son2, sex1);
        Assert.assertEquals(son2.getSex(), sex1);


        // 仅 父类都 有的 private 属性
        Son son3 = new Son();
        String ssss = "d dd d d dd dd";
        Field FieldOnlyFather = EntityUtils.getDeclaredFieldContainParent(Son.class, "onlyFather");
        Assert.assertNotNull(FieldOnlyFather);
        FieldOnlyFather.setAccessible(true);
        FieldOnlyFather.set(son3, ssss);
        Assert.assertEquals(son3.getOnlyFather(), ssss);


    }

    interface Grandfather extends Serializable {
        String name = "grand_father";
        String age = "66";
        String onlyGrandfather = "onlyGrandfather";
    }

    interface GrandMother {
        String name = "grand_mother";
        String age = "61";
        String onlyGrandMother = "onlyGrandMother";
    }

    @Data
    abstract static class Father implements Grandfather, GrandMother {
        private static final long serialVersionUID = 1L;
        protected Integer sex = 1;
        private String name = "father";
        private Integer age = 36;
        private String onlyFather = "onlyFather";
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    static class Son extends Father {
        private static final long serialVersionUID = 1L;
        private String name = "son";
        private Integer age = 14;
        private String onlySon = "onlySon";
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    static class Daughter extends Father {
        private static final long serialVersionUID = 1L;
        protected Integer sex = 0;
        private String name = "daughter";
        private Integer age = 12;
        private String onlyDaughter = "onlyDaughter";
    }

}
