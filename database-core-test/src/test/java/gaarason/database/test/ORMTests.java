package gaarason.database.test;

//import com.fasterxml.jackson.core.JsonProcessingException;
import gaarason.database.connections.ProxyDataSource;
import gaarason.database.eloquent.Record;
import gaarason.database.test.models.StudentORMModel;
import gaarason.database.test.parent.BaseTests;
import gaarason.database.test.relation.data.model.StudentModel;
import gaarason.database.test.relation.data.model.TeacherModel;
import gaarason.database.test.relation.data.pojo.Student;
import gaarason.database.test.relation.data.pojo.Teacher;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.sql.DataSource;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
public class ORMTests extends BaseTests {

    private static StudentORMModel studentORMModel = new StudentORMModel();

    private static StudentModel studentModel = new StudentModel();

    private static TeacherModel teacherModel = new TeacherModel();

    protected List<DataSource> getDataSourceList() {
        ProxyDataSource proxyDataSource = studentORMModel.getDataSource();
        return proxyDataSource.getMasterDataSourceList();
    }

    @Test
    public void ORM查询() {
        Record<StudentORMModel.Entity, Integer> record = studentORMModel.findOrFail(3);
        StudentORMModel.Entity                  entity = record.getEntity();
        Assert.assertEquals(entity.getAge(), Byte.valueOf("16"));
        Assert.assertEquals(entity.getName(), "小腾");

        Record<StudentORMModel.Entity, Integer> noRecord = studentORMModel.find(32);
        Assert.assertNull(noRecord);

        Record<StudentORMModel.Entity, Integer> record2 = studentORMModel.find(9);
        Assert.assertNotNull(record2);
        StudentORMModel.Entity entity2 = record2.getEntity();
        Assert.assertEquals(entity2.getAge(), Byte.valueOf("17"));
        Assert.assertEquals(entity2.getName(), "莫西卡");

    }

    @Test
    public void ORM更新() {
        Record<StudentORMModel.Entity, Integer> record = studentORMModel.findOrFail(8);
        record.getEntity().setAge(Byte.valueOf("121"));
        boolean save = record.save();
        Assert.assertTrue(save);

        Byte age = studentORMModel.newQuery().where("id", "8").firstOrFail().toObject().getAge();
        Assert.assertEquals(age, Byte.valueOf("121"));

        // 成功更新后自身属性被刷新
        StudentORMModel.Entity entity = record.toObject();
        Assert.assertEquals(entity.getAge(), Byte.valueOf("121"));

//        String s = record.toJson();
//        System.out.println(s);

        // ID 为 9 的不可以更新
        Record<StudentORMModel.Entity, Integer> record2 = studentORMModel.findOrFail(9);
        record2.getEntity().setAge(Byte.valueOf("121"));
        boolean save2 = record2.save();
        Assert.assertFalse(save2);

        Byte age2 = studentORMModel.newQuery().where("id", "9").firstOrFail().toObject().getAge();
        Assert.assertNotEquals(age2, Byte.valueOf("121"));
    }

    @Test
    public void ORM新增() {
        Record<StudentORMModel.Entity, Integer> record = studentORMModel.newRecord();
        StudentORMModel.Entity                  entity = record.getEntity();
        entity.setId(15);
        entity.setName("小超超");
        entity.setAge(Byte.valueOf("44"));
        entity.setSex(Byte.valueOf("1"));
        entity.setTeacherId(1);
        boolean save = record.save();
        Assert.assertTrue(save);

        System.out.println(record);

        Record<StudentORMModel.Entity, Integer> r = studentORMModel.findOrFail(15);
        Assert.assertEquals(r.toObject().getName(), "小超超");

        // 成功新增后自身属性被刷新
        record.getEntity().setTeacherId(33);
        boolean save1 = record.save();
        Assert.assertTrue(save1);

        Assert.assertEquals(record.toObject().getName(), "小超超");
        Assert.assertEquals(record.toObject().getTeacherId().intValue(), 33);

        // 用查询构造器验证
        Record<StudentORMModel.Entity, Integer> record1 = studentORMModel.newQuery().where("id", "15").firstOrFail();
        Assert.assertEquals(record1.toObject().getTeacherId().intValue(), 33);
        Assert.assertEquals(record1.toObject().getName(), "小超超");
    }

    @Test
    public void ORM新增_自增id() {
        Record<StudentORMModel.Entity, Integer> record = studentORMModel.newRecord();
        StudentORMModel.Entity                  entity = record.getEntity();
        entity.setName("小超超");
        entity.setAge(Byte.valueOf("44"));
        entity.setSex(Byte.valueOf("1"));
        entity.setTeacherId(1);
        boolean save = record.save();
        Assert.assertTrue(save);
        System.out.println(record);
        Record<StudentORMModel.Entity, Integer> r = studentORMModel.findOrFail(20);
        Assert.assertEquals(r.toObject().getName(), "小超超");
        Assert.assertEquals(record.getEntity().getId().intValue(), 20);
        Assert.assertEquals(entity.getId().intValue(), 20);

        // 成功新增后自身属性被刷新
        record.getEntity().setTeacherId(33);
        boolean save1 = record.save();
        Assert.assertTrue(save1);

        Assert.assertEquals(record.toObject().getName(), "小超超");
        Assert.assertEquals(record.toObject().getTeacherId().intValue(), 33);

        // 用查询构造器验证
        Record<StudentORMModel.Entity, Integer> record1 = studentORMModel.newQuery().where("id", "20").firstOrFail();
        Assert.assertEquals(record1.toObject().getTeacherId().intValue(), 33);
        Assert.assertEquals(record1.toObject().getName(), "小超超");
    }

    @Test
    public void ORM软删除与恢复() {
        Record<StudentORMModel.Entity, Integer> record = studentORMModel.findOrFail(6);

        boolean delete = record.delete();
        Assert.assertTrue(delete);

        Record<StudentORMModel.Entity, Integer> record1 = studentORMModel.find(6);
        Assert.assertNull(record1);

        boolean restore = record.restore();
        Assert.assertTrue(restore);
        Assert.assertFalse(record.toObject().isDeleted());

        int size = studentORMModel.onlyTrashed().get().toObjectList().size();
        Assert.assertEquals(size, 0);

    }

    @Test
    public void ORM多线程兼容性() throws InterruptedException {
        int            count          = 100;
        CountDownLatch countDownLatch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            Record<StudentORMModel.Entity, Integer> record = studentORMModel.findOrFail(3);
            StudentORMModel.Entity                  entity = record.getEntity();
            Assert.assertEquals(entity.getAge(), Byte.valueOf("16"));
            Assert.assertEquals(entity.getName(), "小腾");

            Record<StudentORMModel.Entity, Integer> noRecord = studentORMModel.find(32);
            Assert.assertNull(noRecord);

            Record<StudentORMModel.Entity, Integer> record2 = studentORMModel.find(9);
            Assert.assertNotNull(record2);
            StudentORMModel.Entity entity2 = record2.getEntity();
            Assert.assertEquals(entity2.getAge(), Byte.valueOf("17"));
            Assert.assertEquals(entity2.getName(), "莫西卡");

            countDownLatch.countDown();
        }
        countDownLatch.await();
    }

    @Test
    public void ORM新增_一对一(){

        String newName = "肖邦";
        String newTeacherName = "肖邦de老师";

        // 先获取新的 record
        Teacher teacher = teacherModel.getEntity();
        teacher.setName(newTeacherName);
        Student student = new Student();
        student.setName(newName);
        teacher.setStudent(student);

        teacherModel.save();

        Student studentCheck = studentModel.newQuery().where("name", newName).with("teacher").firstOrFail().toObject();
        System.out.println(studentCheck);
        Assert.assertEquals(studentCheck.getName(), newName);
        // todo
//        Assert.assertNotNull(student.getTeacher());
//        Assert.assertEquals(student.getTeacher().getName(), newTeacherName);

    }

    @Test
    public void ORM新增_反向一对一(){

        String newName = "肖邦";
        String newTeacherName = "肖邦de老师";

        // 先获取新的 record
        Record<Student, Long> record = studentModel.newRecord();
        Student student1 = record.getEntity();
        student1.setName(newName);
        Teacher teacher = new Teacher();
        teacher.setName(newTeacherName);
        student1.setTeacher(teacher);
        record.save();

        Student student = studentModel.newQuery().where("name", newName).with("teacher").firstOrFail().toObject();
        System.out.println(student);
        Assert.assertEquals(student.getName(), newName);
        // todo
//        Assert.assertNotNull(student.getTeacher());
//        Assert.assertEquals(student.getTeacher().getName(), newTeacherName);

    }
}
