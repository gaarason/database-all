package gaarason.database.test;

import gaarason.database.util.EntityUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;

public class EntityUtilsTests {

    interface Grandfather extends Serializable {
        String name = "grand_father";
        String age = "66";
        String onlyGrandfather = "onlyGrandfather";
    }

    interface GrandMother{
        String name = "grand_mother";
        String age = "61";
        String onlyGrandMother = "onlyGrandMother";
    }

    @Data
    abstract static class Father implements Grandfather, GrandMother{
        private static final long serialVersionUID = 1L;
        private  String name = "father";
        private Integer age = 36;
        protected Integer sex = 1;
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
        private String name = "daughter";
        private Integer age = 12;
        protected Integer sex = 0;
        private String onlyDaughter = "onlyDaughter";
    }


    @Test
    public void getDeclaredFieldsContainParentTest(){
        List<Field> fields = EntityUtils.getDeclaredFieldsContainParent(Son.class);
        Assert.assertEquals(fields.size(), 9);
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

}
