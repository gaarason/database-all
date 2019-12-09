package gaarason.database.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import gaarason.database.connections.ProxyDataSource;
import gaarason.database.eloquent.Record;
import gaarason.database.test.models.StudentORMModel;
import gaarason.database.test.parent.BaseTests;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.MethodSorters;

import javax.sql.DataSource;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
public class ORMTests extends BaseTests {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static StudentORMModel studentModel = new StudentORMModel();

    protected List<DataSource> getDataSourceList() {
        ProxyDataSource proxyDataSource = studentModel.getProxyDataSource();
        return proxyDataSource.getMasterDataSourceList();
    }

    @Test
    public void ORM查询() {
        Record<StudentORMModel.Entity> record = studentModel.findOrFail("3");
        StudentORMModel.Entity         entity = record.getEntity();
        Assert.assertEquals(entity.getAge(), Byte.valueOf("16"));
        Assert.assertEquals(entity.getName(), "小腾");

        Record<StudentORMModel.Entity> noRecord = studentModel.find("32");
        Assert.assertNull(noRecord);

        Record<StudentORMModel.Entity> record2 = studentModel.find("9");
        Assert.assertNotNull(record2);
        StudentORMModel.Entity entity2 = record2.getEntity();
        Assert.assertEquals(entity2.getAge(), Byte.valueOf("17"));
        Assert.assertEquals(entity2.getName(), "莫西卡");

    }

    @Test
    public void ORM更新() throws JsonProcessingException {
        Record<StudentORMModel.Entity> record = studentModel.findOrFail("8");
        record.getEntity().setAge(Byte.valueOf("121"));
        boolean save = record.save();
        Assert.assertTrue(save);

        Byte age = studentModel.newQuery().where("id", "8").firstOrFail().toObject().getAge();
        Assert.assertEquals(age, Byte.valueOf("121"));

        // 成功更新后自身属性被刷新
        StudentORMModel.Entity entity = record.toObject();
        Assert.assertEquals(entity.getAge(), Byte.valueOf("121"));

        String s = record.toJson();
        System.out.println(s);

        // ID 为 9 的不可以更新
        Record<StudentORMModel.Entity> record2 = studentModel.findOrFail("9");
        record2.getEntity().setAge(Byte.valueOf("121"));
        boolean save2 = record2.save();
        Assert.assertFalse(save2);

        Byte age2 = studentModel.newQuery().where("id", "9").firstOrFail().toObject().getAge();
        Assert.assertNotEquals(age2, Byte.valueOf("121"));
    }

    @Test
    public void ORM新增() {
        Record<StudentORMModel.Entity> record = studentModel.newRecord();
        StudentORMModel.Entity         entity = record.getEntity();
        entity.setId(15);
        entity.setName("小超超");
        entity.setAge(Byte.valueOf("44"));
        entity.setSex(Byte.valueOf("1"));
        entity.setTeacherId(1);
        boolean save = record.save();
        Assert.assertTrue(save);

        Record<StudentORMModel.Entity> r = studentModel.findOrFail("15");
        Assert.assertEquals(r.toObject().getName(), "小超超");

        // 成功新增后自身属性被刷新
        record.getEntity().setTeacherId(33);
        boolean save1 = record.save();
        Assert.assertTrue(save1);

        Assert.assertEquals(record.toObject().getName(), "小超超");
        Assert.assertEquals(record.toObject().getTeacherId().intValue(), 33);

        // 用查询构造器验证
        Record<StudentORMModel.Entity> record1 = studentModel.newQuery().where("id", "15").firstOrFail();
        Assert.assertEquals(record1.toObject().getTeacherId().intValue(), 33);
        Assert.assertEquals(record1.toObject().getName(), "小超超");
    }

    @Test
    public void ORM软删除与恢复() {
        Record<StudentORMModel.Entity> record = studentModel.findOrFail("6");

        boolean delete = record.delete();
        Assert.assertTrue(delete);

        Record<StudentORMModel.Entity> record1 = studentModel.find("6");
        Assert.assertNull(record1);

        boolean restore = record.restore();
        Assert.assertTrue(restore);
        Assert.assertFalse(record.toObject().isDeleted());

        int size = studentModel.onlyTrashed().get().toObjectList().size();
        Assert.assertEquals(size, 0);

    }

    @Test
    public void ORM多线程兼容性() throws InterruptedException {
        int            count          = 100;
        CountDownLatch countDownLatch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            Record<StudentORMModel.Entity> record = studentModel.findOrFail("3");
            StudentORMModel.Entity         entity = record.getEntity();
            Assert.assertEquals(entity.getAge(), Byte.valueOf("16"));
            Assert.assertEquals(entity.getName(), "小腾");

            Record<StudentORMModel.Entity> noRecord = studentModel.find("32");
            Assert.assertNull(noRecord);

            Record<StudentORMModel.Entity> record2 = studentModel.find("9");
            Assert.assertNotNull(record2);
            StudentORMModel.Entity entity2 = record2.getEntity();
            Assert.assertEquals(entity2.getAge(), Byte.valueOf("17"));
            Assert.assertEquals(entity2.getName(), "莫西卡");

            countDownLatch.countDown();
        }
        countDownLatch.await();
    }
}
