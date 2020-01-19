package gaarason.database.spring.boot.starter.test;

import gaarason.database.eloquent.GeneralModel;
import gaarason.database.eloquent.Record;
import gaarason.database.generator.GeneralGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.JVM)
@Slf4j
public class TestApplicationTests {

    @Resource
    GeneralGenerator generalGenerator;

    @Resource
    GeneralModel generalModel;

    @Test
    public void 生成代码() {
        // 设置
        generalGenerator.setStaticField(true);
        generalGenerator.setIsSpringBoot(false);
        generalGenerator.setOutputDir("./src/main/java/");
        generalGenerator.setNamespace("gaarason.database.test.relation.data");
        String[] disableCreate = {"created_at", "updated_at"};
        generalGenerator.setDisInsertable(disableCreate);
        String[] disableUpdate = {"created_at", "updated_at"};
        generalGenerator.setDisUpdatable(disableUpdate);

        generalGenerator.run();
    }

    @Test
    public void 简单查询_通用() {
        Record<GeneralModel.Table> first = generalModel.newQuery().from("student").where("id", "3").first();
        Assert.assertNotNull(first);
        Map<String, Object> stringObjectMap = first.toMap();
        Assert.assertEquals((long) stringObjectMap.get("id"), 3);
        System.out.println(stringObjectMap);
    }

}