package gaarason.database.generator.test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import gaarason.database.generator.element.base.BaseElement;
import gaarason.database.generator.support.TypeReference;

/**
 * 测试
 * @author xt
 */
@FixMethodOrder(MethodSorters.JVM)
public class UtilTests {

    public static class Integer{

    }

    @Test
    public void baseElementTest() {

        BaseElement baseElement = new BaseElement() {};

        baseElement.setNamespace("gaarason.database.generator.test");

        String field = baseElement.type2Name(new TypeReference<Field>() {});
        Assert.assertEquals("Field", field);

        String str = baseElement.type2Name(new TypeReference<String>() {});
        Assert.assertEquals("String", str);

        String intList = baseElement.type2Name(new TypeReference<List<Integer>>() {});
        Assert.assertEquals("List<UtilTests.Integer>", intList);

        String int2List = baseElement.type2Name(new TypeReference<List<java.lang.Integer>>() {});
        Assert.assertEquals("List<Integer>", int2List);

        String intMapList = baseElement.type2Name(new TypeReference<List<Map<Object, Integer>>>() {});
        Assert.assertEquals("List<Map<Object, UtilTests.Integer>>", intMapList);

        String printImports = baseElement.printImports();
        Assert.assertEquals("import java.lang.reflect.Field;\n" +
            "import java.util.List;\n" +
            "import java.util.Map;", printImports);


        System.out.println("ok");

    }

}
