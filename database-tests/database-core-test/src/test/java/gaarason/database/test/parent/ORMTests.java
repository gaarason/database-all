package gaarason.database.test.parent;

import gaarason.database.appointment.OrderBy;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.exception.RelationAttachException;
import gaarason.database.exception.SQLRuntimeException;
import gaarason.database.test.models.normal.StudentORMModel;
import gaarason.database.test.models.relation.model.RelationshipStudentTeacherModel;
import gaarason.database.test.models.relation.model.StudentModel;
import gaarason.database.test.models.relation.model.TeacherModel;
import gaarason.database.test.models.relation.pojo.RelationshipStudentTeacher;
import gaarason.database.test.models.relation.pojo.Student;
import gaarason.database.test.models.relation.pojo.Teacher;
import gaarason.database.test.parent.base.BaseTests;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.*;
import java.util.concurrent.CountDownLatch;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
abstract public class ORMTests extends BaseTests {

    protected static StudentORMModel studentORMModel = new StudentORMModel();

    protected static StudentModel studentModel = new StudentModel();

    protected static TeacherModel teacherModel = new TeacherModel();

    protected static gaarason.database.test.models.relation.model.StudentModel studentRelationModel =
        new gaarason.database.test.models.relation.model.StudentModel();

    protected static RelationshipStudentTeacherModel relationshipStudentTeacherModel = new RelationshipStudentTeacherModel();

    @Override
    protected GaarasonDataSource getGaarasonDataSource() {
        return studentORMModel.getGaarasonDataSource();
    }
    @Override
    protected List<TABLE> getInitTables() {
        return Arrays.asList(TABLE.student, TABLE.teacher, TABLE.relationship_student_teacher);
    }
    @Test
    public void ORM查询() {
        Record<StudentORMModel.Entity, Integer> record = studentORMModel.findOrFail(3);
        StudentORMModel.Entity entity = record.getEntity();
        Assert.assertEquals(entity.getAge(), Byte.valueOf("16"));
        Assert.assertEquals(entity.getName(), "小腾");

        Record<StudentORMModel.Entity, Integer> noRecordTest = studentORMModel.find("32");
        Assert.assertNull(noRecordTest);
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
    public void ORM更新_saveByPrimaryKey(){
        StudentORMModel.Entity entity0 = studentORMModel.findOrFail(8).toObject();
        entity0.setName("ddddd");
        // 主键冲突
        Assert.assertThrows(SQLRuntimeException.class , ()->{
            studentORMModel.newRecord().fillEntity(entity0).save();
        });


        StudentORMModel.Entity entity = studentORMModel.findOrFail(8).toObject();
        entity.setName("ddddd");
        // 按entity中的主键存在就更新
        boolean b = studentORMModel.newRecord().fillEntity(entity).saveByPrimaryKey();
        Assert.assertTrue(b);

        StudentORMModel.Entity check = studentORMModel.findOrFail(8).toObject();
        Assert.assertEquals("ddddd", check.getName());
    }

    @Test
    public void ORM新增() {
        Record<StudentORMModel.Entity, Integer> record = studentORMModel.newRecord();
        StudentORMModel.Entity entity = record.getEntity();
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
        StudentORMModel.Entity entity = record.getEntity();
        entity.setName("小超超");
        entity.setAge(Byte.valueOf("44"));
        entity.setSex(Byte.valueOf("1"));
        entity.setTeacherId(1);
        Assert.assertNull(entity.getId());
        boolean save = record.save();
        Assert.assertEquals(20, record.getEntity().getId().intValue());
        Assert.assertTrue(save);
        System.out.println(record);
        StudentORMModel.Entity object = record.toObject();
        Assert.assertEquals(20, object.getId().intValue());
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
    public void ORM新增_findOrNew() {
        String name = "findOrCreate的name";
        final StudentORMModel.Entity stu = new StudentORMModel.Entity();
        stu.setName(name);

        final Long oldCount = studentORMModel.newQuery().count();

        // 不存在,所以会新增一条, 但是尚未持久化到数据库
        final Record<StudentORMModel.Entity, Integer> theRecord = studentORMModel.findOrNew(stu);

        final Long newCount = studentORMModel.newQuery().count();
        Assert.assertEquals(oldCount, newCount);

        // 手动持久化
        theRecord.save();
        final Long newCountOther = studentORMModel.newQuery().count();
        Assert.assertEquals(oldCount + 1L, newCountOther.longValue());

        final StudentORMModel.Entity entity = theRecord.toObject();
        Assert.assertEquals(entity.getName(), name);


        //-------------
        // 已经存在,所以仅会查询
        final Record<StudentORMModel.Entity, Integer> theRecord2 = studentORMModel.findOrNew(stu);

        final Long newCount2 = studentORMModel.newQuery().count();

        final StudentORMModel.Entity entity2 = theRecord2.toObject();
        Assert.assertEquals(entity2.getName(), name);

        Assert.assertEquals(newCountOther.longValue(), newCount2.longValue());
    }

    @Test
    public void ORM新增_findByPrimaryKeyOrNew() {
        String name = "findOrCreate的name";
        Integer id = 456;
        final StudentORMModel.Entity stu = new StudentORMModel.Entity();
        stu.setId(id);
        stu.setName(name);

        final Long oldCount = studentORMModel.newQuery().count();

        // 不存在,所以会新增一条, 但是尚未持久化到数据库
        final Record<StudentORMModel.Entity, Integer> theRecord = studentORMModel.findByPrimaryKeyOrNew(stu);

        final Long newCount = studentORMModel.newQuery().count();
        Assert.assertEquals(oldCount, newCount);

        // 手动持久化
        theRecord.save();
        final Long newCountOther = studentORMModel.newQuery().count();
        Assert.assertEquals(oldCount + 1L, newCountOther.longValue());

        final StudentORMModel.Entity entity = theRecord.toObject();
        Assert.assertEquals(entity.getName(), name);


        //-------------
        // 已经存在,所以仅会查询
        final Record<StudentORMModel.Entity, Integer> theRecord2 = studentORMModel.findByPrimaryKeyOrNew(stu);

        final Long newCount2 = studentORMModel.newQuery().count();

        final StudentORMModel.Entity entity2 = theRecord2.toObject();
        Assert.assertEquals(entity2.getName(), name);
        Assert.assertEquals(entity2.getId(), id);

        Assert.assertEquals(newCountOther.longValue(), newCount2.longValue());
    }

    @Test
    public void ORM新增_findOrCreate() {
        String name = "findOrCreate的name";
        final StudentORMModel.Entity stu = new StudentORMModel.Entity();
        stu.setName(name);

        final Long oldCount = studentORMModel.newQuery().count();

        // 不存在,所以会新增一条
        final Record<StudentORMModel.Entity, Integer> theRecord = studentORMModel.findOrCreate(stu);

        final Long newCount = studentORMModel.newQuery().count();

        final StudentORMModel.Entity entity = theRecord.toObject();
        Assert.assertEquals(entity.getName(), name);

        Assert.assertEquals(oldCount + 1L, newCount.longValue());

        //-------------
        // 已经存在,所以仅会查询
        final Record<StudentORMModel.Entity, Integer> theRecord2 = studentORMModel.findOrCreate(stu);

        final Long newCount2 = studentORMModel.newQuery().count();

        final StudentORMModel.Entity entity2 = theRecord2.toObject();
        Assert.assertEquals(entity2.getName(), name);

        Assert.assertEquals(newCount.longValue(), newCount2.longValue());
    }

    @Test
    public void ORM新增_findByPrimaryKeyOrCreate() {
        String name = "theName";
        Integer id = 99;
        final StudentORMModel.Entity stu = new StudentORMModel.Entity();
        stu.setId(id);
        stu.setName(name);

        final Long oldCount = studentORMModel.newQuery().count();

        // 不存在,所以会新增一条
        final Record<StudentORMModel.Entity, Integer> theRecord = studentORMModel.findByPrimaryKeyOrCreate(stu);

        final Long newCount = studentORMModel.newQuery().count();

        final StudentORMModel.Entity entity = theRecord.toObject();
        Assert.assertEquals(entity.getName(), name);

        Assert.assertEquals(oldCount + 1L, newCount.longValue());

        //-------------
        // 已经存在,所以仅会查询
        final Record<StudentORMModel.Entity, Integer> theRecord2 = studentORMModel.findByPrimaryKeyOrCreate(stu);

        final Long newCount2 = studentORMModel.newQuery().count();

        final StudentORMModel.Entity entity2 = theRecord2.toObject();
        Assert.assertEquals(entity2.getName(), name);

        Assert.assertEquals(newCount.longValue(), newCount2.longValue());

        //----------------
        // 按id可以找到, 即使其他属性不一致
        final StudentORMModel.Entity stu2 = new StudentORMModel.Entity();
        stu2.setId(id);
        stu2.setName("name");

        final Record<StudentORMModel.Entity, Integer> theRecord3 = studentORMModel.findByPrimaryKeyOrCreate(stu);
        final Long newCount3 = studentORMModel.newQuery().count();

        final StudentORMModel.Entity entity3 = theRecord3.toObject();
        // 等于数据库中的值
        Assert.assertEquals(entity3.getName(), name);

        Assert.assertEquals(newCount.longValue(), newCount3.longValue());
    }

    @Test
    public void ORM新增_findOrNew_2() {
        String name = "findOrCreate的name";
        Byte age = Byte.valueOf("3");
        Byte age3 = Byte.valueOf("33");

        final StudentORMModel.Entity stu1 = new StudentORMModel.Entity();
        stu1.setName(name);
        final StudentORMModel.Entity stu2 = new StudentORMModel.Entity();
        stu2.setAge(age);
        // 原有的记录数
        final Long oldCount = studentORMModel.newQuery().count();
        // 因为没有存在满足条件的记录, 所以新增
        final Record<StudentORMModel.Entity, Integer> theRecord = studentORMModel.findOrNew(stu1, stu2);
        // 未持久化,所以数据库条数不变
        final Long newC = studentORMModel.newQuery().count();
        Assert.assertEquals(oldCount, newC);

        // 手动持久化
        theRecord.save();
        // 新的记录数
        final Long newCount = studentORMModel.newQuery().count();
        final StudentORMModel.Entity stuEntity1 = theRecord.toObject();
        Assert.assertEquals(stuEntity1.getName(), name);
        Assert.assertEquals(stuEntity1.getAge(), age);
        Assert.assertEquals(oldCount + 1L, newCount.longValue());

        // 因为已经存在满足条件的, 所以只是查询, 且"插入补充信息的对象属性"不会使用
        final StudentORMModel.Entity stu3 = new StudentORMModel.Entity();
        stu3.setAge(age3);
        final Record<StudentORMModel.Entity, Integer> theRecord2 = studentORMModel.findOrNew(stu1, stu2);
        // 更加新的记录数
        final Long newCount2 = studentORMModel.newQuery().count();
        final StudentORMModel.Entity entity2 = theRecord2.toObject();

        Assert.assertEquals(name, entity2.getName());
        Assert.assertEquals(age, entity2.getAge());
        Assert.assertNotEquals(age3, entity2.getAge());
        Assert.assertEquals(newCount, newCount2);
    }

    @Test
    public void ORM新增_findOrCreate_2() {
        String name = "findOrCreate的name";
        Byte age = Byte.valueOf("3");
        Byte age3 = Byte.valueOf("33");

        final StudentORMModel.Entity stu1 = new StudentORMModel.Entity();
        stu1.setName(name);
        final StudentORMModel.Entity stu2 = new StudentORMModel.Entity();
        stu2.setAge(age);
        // 原有的记录数
        final Long oldCount = studentORMModel.newQuery().count();
        // 因为没有存在满足条件的记录, 所以新增
        final Record<StudentORMModel.Entity, Integer> theRecord = studentORMModel.findOrCreate(stu1, stu2);
        // 新的记录数
        final Long newCount = studentORMModel.newQuery().count();
        final StudentORMModel.Entity stuEntity1 = theRecord.toObject();
        Assert.assertEquals(stuEntity1.getName(), name);
        Assert.assertEquals(stuEntity1.getAge(), age);
        Assert.assertEquals(oldCount + 1L, newCount.longValue());

        // 因为已经存在满足条件的, 所以只是查询, 且"插入补充信息的对象属性"不会使用
        final StudentORMModel.Entity stu3 = new StudentORMModel.Entity();
        stu3.setAge(age3);
        final Record<StudentORMModel.Entity, Integer> theRecord2 = studentORMModel.findOrCreate(stu1, stu2);
        // 更加新的记录数
        final Long newCount2 = studentORMModel.newQuery().count();
        final StudentORMModel.Entity entity2 = theRecord2.toObject();

        Assert.assertEquals(entity2.getName(), name);
        Assert.assertEquals(entity2.getAge(), age);
        Assert.assertNotEquals(entity2.getAge(), age3);
        Assert.assertEquals(newCount2, newCount);
    }

    @Test
    public void ORM新增_updateByPrimaryKeyOrCreate() {
        String name = "findOrCreate的name";
        Byte age = Byte.valueOf("3");
        Integer id = 7892;
        final StudentORMModel.Entity stu1 = new StudentORMModel.Entity();
        stu1.setId(id);
        stu1.setName(name);
        stu1.setAge(age);

        // 原有的记录数
        final Long oldCount = studentORMModel.newQuery().count();
        // 因为没有记录满足, 所以新增
        final Record<StudentORMModel.Entity, Integer> theRecord = studentORMModel.updateByPrimaryKeyOrCreate(stu1);
        final StudentORMModel.Entity entity = theRecord.toObject();
        // 新的的记录数
        final Long newCount = studentORMModel.newQuery().count();
        Assert.assertEquals(oldCount + 1L, newCount.longValue());
        // 新增后的数据就是 stu1 + stu2
        Assert.assertEquals(entity.getName(), name);
        Assert.assertEquals(entity.getAge(), age);

        // --------------
        Byte age2 = Byte.valueOf("33");
        final StudentORMModel.Entity stu2 = new StudentORMModel.Entity();
        stu2.setId(id);
        stu2.setAge(age2);

        // 因为存在记录满足, 所以更新
        final StudentORMModel.Entity entity2 = studentORMModel.updateByPrimaryKeyOrCreate(stu2).toObject();
        // 新的的记录数2
        final Long newCount2 = studentORMModel.newQuery().count();
        Assert.assertEquals(newCount, newCount2);
        // 更新后的数据就是 stu1 + stu3
        Assert.assertEquals(entity2.getName(), name);
        Assert.assertEquals(entity2.getAge(), age2);

    }

    @Test
    public void ORM新增_updateOrCreate() {
        String name = "findOrCreate的name";
        Byte age = Byte.valueOf("3");
        Byte age3 = Byte.valueOf("33");
        final StudentORMModel.Entity stu1 = new StudentORMModel.Entity();
        stu1.setName(name);
        final StudentORMModel.Entity stu2 = new StudentORMModel.Entity();
        stu2.setAge(age);

        // 原有的记录数
        final Long oldCount = studentORMModel.newQuery().count();
        // 因为没有记录满足, 所以新增
        final Record<StudentORMModel.Entity, Integer> theRecord = studentORMModel.updateOrCreate(stu1, stu2);
        final StudentORMModel.Entity entity = theRecord.toObject();
        // 新的的记录数
        final Long newCount = studentORMModel.newQuery().count();
        Assert.assertEquals(oldCount + 1L, newCount.longValue());
        // 新增后的数据就是 stu1 + stu2
        Assert.assertEquals(entity.getName(), name);
        Assert.assertEquals(entity.getAge(), age);

        // --------------
        final StudentORMModel.Entity stu3 = new StudentORMModel.Entity();
        stu3.setAge(age3);
        // 因为存在记录满足, 所以更新
        final StudentORMModel.Entity entity3 = studentORMModel.updateOrCreate(stu1, stu3).toObject();
        // 新的的记录数2
        final Long newCount2 = studentORMModel.newQuery().count();
        Assert.assertEquals(newCount, newCount2);
        // 更新后的数据就是 stu1 + stu3
        Assert.assertEquals(entity3.getName(), name);
        Assert.assertEquals(entity3.getAge(), age3);
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
        Assert.assertFalse(record.toObject().getIsDeleted());

        int size = studentORMModel.onlyTrashed().get().toObjectList().size();
        Assert.assertEquals(size, 0);

    }

    @Test
    public void ORM多线程兼容性() throws InterruptedException {
        int count = 100;
        CountDownLatch countDownLatch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            Record<StudentORMModel.Entity, Integer> record = studentORMModel.findOrFail(3);
            StudentORMModel.Entity entity = record.getEntity();
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
    public void attach_BelongsToMany_单个() {
        Record<Teacher, Long> teacherIntegerRecord = teacherModel.findOrFail(1L);

        Teacher studentsBelongsToMany = teacherIntegerRecord.with("studentsBelongsToMany").toObject();
        // 老师(id=1)已经有3个学生(id=1, id=2, id=3)
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().size(), 3);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(0).getId().intValue(), 1);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(1).getId().intValue(), 2);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(2).getId().intValue(), 3);

        Long oldCount = relationshipStudentTeacherModel.newQuery().count("*");

        Record<Student, Long> student = studentModel.findOrFail(2L);
        teacherIntegerRecord.bind("studentsBelongsToMany").attach(student);

        Long new1Count = relationshipStudentTeacherModel.newQuery().count("*");

        // 因为, 老师(id=1)已经有3个学生(id=1, id=2, id=3), 所以增加学生(id=2)不会产生任何操作
        Assert.assertEquals(new1Count - oldCount, 0);

        Record<Student, Long> student2 = studentModel.findOrFail(4L);
        teacherIntegerRecord.bind("studentsBelongsToMany").attach(student2);
        Long new2Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(new2Count - oldCount, 1);

        Teacher teacher = teacherIntegerRecord.toObject();
        Assert.assertEquals(teacher.getStudentsBelongsToMany().size(), 4);
    }

    @Test
    public void attach_BelongsToMany_单个_附带信息到中间表() {
        String note = "ssssss";
        Record<Teacher, Long> teacherIntegerRecord = teacherModel.findOrFail(1L);

        Teacher studentsBelongsToMany = teacherIntegerRecord.with("studentsBelongsToMany")
            .with("relationshipStudentTeachers")
            .toObject();
        // 老师(id=1)已经有3个学生(id=1, id=2, id=3)
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().size(), 3);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(0).getId().intValue(), 1);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(1).getId().intValue(), 2);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(2).getId().intValue(), 3);

        Long oldCount = relationshipStudentTeacherModel.newQuery().count("*");

        Record<Student, Long> student = studentModel.findOrFail(2L);
        teacherIntegerRecord.bind("studentsBelongsToMany").attach(student);

        Long new1Count = relationshipStudentTeacherModel.newQuery().count("*");

        // 因为, 老师(id=1)已经有3个学生(id=1, id=2, id=3), 所以增加学生(id=2)不会产生任何操作
        Assert.assertEquals(new1Count - oldCount, 0);

        Record<Student, Long> student2 = studentModel.findOrFail(4L);

        HashMap<String, Object> map = new HashMap<>();
        map.put("note", note);
        teacherIntegerRecord.bind("studentsBelongsToMany").attach(student2, map);
        Long new2Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(new2Count - oldCount, 1);

        RelationshipStudentTeacher relationshipStudentTeacher = relationshipStudentTeacherModel.newQuery()
            .where("student_id", "4")
            .where("teacher_id", "1")
            .firstOrFail()
            .toObject();
        Assert.assertEquals(relationshipStudentTeacher.getNote(), note);

        Teacher teacher = teacherIntegerRecord.toObject();
        Assert.assertEquals(teacher.getStudentsBelongsToMany().size(), 4);
        Assert.assertEquals(teacher.getRelationshipStudentTeachers().size(), 4);
        Assert.assertEquals(teacher.getRelationshipStudentTeachers().get(3).getNote(), note);
    }

    @Test
    public void attach_BelongsToMany_批量_附带信息到中间表() {
        String note = "ssssss";
        Record<Teacher, Long> teacherIntegerRecord = teacherModel.findOrFail(1L);

        Teacher studentsBelongsToMany = teacherIntegerRecord.with("studentsBelongsToMany")
            .with("relationshipStudentTeachers")
            .toObject();
        // 老师(id=1)已经有3个学生(id=1, id=2, id=3)
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().size(), 3);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(0).getId().intValue(), 1);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(1).getId().intValue(), 2);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(2).getId().intValue(), 3);

        Long oldCount = relationshipStudentTeacherModel.newQuery().count("*");

        RecordList<Student, Long> student =
            studentModel.newQuery().whereIn("id", "1", "2", "3", "4", "5").get();
        Assert.assertEquals(student.size(), 5);


        HashMap<String, Object> map = new HashMap<>();
        map.put("note", note);
        teacherIntegerRecord.bind("studentsBelongsToMany").attach(student, map);

        Long new2Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(new2Count - oldCount, 2);

        List<RelationshipStudentTeacher> relationshipStudentTeacher = relationshipStudentTeacherModel.newQuery()
            .whereIn("student_id", "4", "5")
            .where("teacher_id", "1")
            .get()
            .toObjectList();
        System.out.println(relationshipStudentTeacher);
        Assert.assertEquals(relationshipStudentTeacher.get(0).getNote(), note);
        Assert.assertEquals(relationshipStudentTeacher.get(1).getNote(), note);

        Teacher teacher = teacherIntegerRecord.toObject();
        Assert.assertEquals(teacher.getStudentsBelongsToMany().size(), 5);
        Assert.assertEquals(teacher.getRelationshipStudentTeachers().size(), 5);
        Assert.assertNotEquals(teacher.getRelationshipStudentTeachers().get(2).getNote(), note);
        Assert.assertEquals(teacher.getRelationshipStudentTeachers().get(3).getNote(), note);
        Assert.assertEquals(teacher.getRelationshipStudentTeachers().get(4).getNote(), note);
    }

    @Test
    public void attach_BelongsToMany_批量id() {
        String note = "ssssss";
        Record<Teacher, Long> teacherIntegerRecord = teacherModel.findOrFail(1L);

        Teacher studentsBelongsToMany = teacherIntegerRecord.with("studentsBelongsToMany")
            .with("relationshipStudentTeachers")
            .toObject();
        // 老师(id=1)已经有3个学生(id=1, id=2, id=3)
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().size(), 3);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(0).getId().intValue(), 1);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(1).getId().intValue(), 2);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(2).getId().intValue(), 3);

        Long oldCount = relationshipStudentTeacherModel.newQuery().count("*");

        teacherIntegerRecord.bind("studentsBelongsToMany").attach(Arrays.asList(1L, 2L, 3L, 4L, 5L));

        Long new2Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(new2Count - oldCount, 2);

        List<RelationshipStudentTeacher> relationshipStudentTeacher = relationshipStudentTeacherModel.newQuery()
            .whereIn("student_id", "4", "5")
            .where("teacher_id", "1")
            .get()
            .toObjectList();
        System.out.println(relationshipStudentTeacher);
        Assert.assertEquals(relationshipStudentTeacher.size(), 2);

        Teacher teacher = teacherIntegerRecord.toObject();
        Assert.assertEquals(teacher.getStudentsBelongsToMany().size(), 5);
        Assert.assertEquals(teacher.getRelationshipStudentTeachers().size(), 5);
        Assert.assertEquals(teacher.getRelationshipStudentTeachers().get(3).getNote(), "");
        Assert.assertEquals(teacher.getRelationshipStudentTeachers().get(4).getNote(), "");
    }


    @Test
    public void attach_BelongsToMany_批量id_附带信息到中间表() {
        String note = "ssssss";
        Record<Teacher, Long> teacherIntegerRecord = teacherModel.findOrFail(1L);

        Teacher studentsBelongsToMany = teacherIntegerRecord.with("studentsBelongsToMany")
            .with("relationshipStudentTeachers")
            .toObject();
        // 老师(id=1)已经有3个学生(id=1, id=2, id=3)
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().size(), 3);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(0).getId().intValue(), 1);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(1).getId().intValue(), 2);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(2).getId().intValue(), 3);

        Long oldCount = relationshipStudentTeacherModel.newQuery().count("*");

        HashMap<String, Object> map = new HashMap<>();
        map.put("note", note);
        teacherIntegerRecord.bind("studentsBelongsToMany").attach(Arrays.asList(1L, 2L, 3L, 4L, 5L), map);

        Long new2Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(new2Count - oldCount, 2);

        List<RelationshipStudentTeacher> relationshipStudentTeacher = relationshipStudentTeacherModel.newQuery()
            .whereIn("student_id", "4", "5")
            .where("teacher_id", "1")
            .get()
            .toObjectList();
        System.out.println(relationshipStudentTeacher);
        Assert.assertEquals(relationshipStudentTeacher.get(0).getNote(), note);
        Assert.assertEquals(relationshipStudentTeacher.get(1).getNote(), note);

        Teacher teacher = teacherIntegerRecord.toObject();
        Assert.assertEquals(teacher.getStudentsBelongsToMany().size(), 5);
        Assert.assertEquals(teacher.getRelationshipStudentTeachers().size(), 5);
        Assert.assertNotEquals(teacher.getRelationshipStudentTeachers().get(2).getNote(), note);
        Assert.assertEquals(teacher.getRelationshipStudentTeachers().get(3).getNote(), note);
        Assert.assertEquals(teacher.getRelationshipStudentTeachers().get(4).getNote(), note);
    }

    @Test
    public void attach_BelongsTo_单个() {
        Record<Student, Long> studentLongRecord = studentRelationModel.findOrFail(1L);

        Record<Teacher, Long> teacherIntegerRecord = teacherModel.findOrFail(1L);

        Student student = studentLongRecord.with("teacher").toObject();

        Assert.assertEquals(student.getTeacher().getId().intValue(), 6);

        studentLongRecord.bind("teacher").attach(teacherIntegerRecord);

        Student student1 = studentLongRecord.toObject();

        Assert.assertEquals(student1.getTeacher().getId().intValue(), 1);
    }

    @Test
    public void attach_BelongsTo_单个id() {
        Record<Student, Long> studentLongRecord = studentRelationModel.findOrFail(1L);

        Student student = studentLongRecord.with("teacher").toObject();

        Assert.assertEquals(student.getTeacher().getId().intValue(), 6);

        studentLongRecord.bind("teacher").attach("1");

        Student student1 = studentLongRecord.toObject();

        Assert.assertEquals(student1.getTeacher().getId().intValue(), 1);
    }

    @Test
    public void attach_BelongsTo_异常() {
        Record<Student, Long> studentLongRecord = studentRelationModel.findOrFail(1L);

        RecordList<Teacher, Long> all = teacherModel.findAll();

        Student student = studentLongRecord.with("teacher").toObject();


        Assert.assertThrows(RelationAttachException.class, () -> {
            studentLongRecord.bind("teacher").attach(all);
        });

    }


    @Test
    public void attach_HasOneOrMany_单体_单个() {
        Record<Teacher, Long> teacherRecord = teacherModel.findOrFail(1L);

        // 先清除student
        int update = studentModel.newQuery().where("teacher_id", "1").data("teacher_id", "9").update();
        Assert.assertTrue(update > 0);

        Teacher teacher1 = teacherRecord.with("student").toObject();
        System.out.println(teacher1);
        // 老师(id=1)已经有没有学生
        Assert.assertNull(teacher1.getStudent());

        // id=5的学生已经存在,所以应该Update执行没有影响
        Record<Student, Long> student = studentModel.findOrFail(5L);
        teacherRecord.bind("student").attach(student);

        Teacher teacher2 = teacherRecord.toObject();
        Assert.assertEquals(teacher2.getStudent().getId().intValue(), 5);
    }

    @Test
    public void attach_HasOneOrMany_单体_多个() {
        Record<Teacher, Long> teacherRecord = teacherModel.findOrFail(1L);

        // 先清除student
        int update = studentModel.newQuery().where("teacher_id", "1").data("teacher_id", "9").update();
        Assert.assertTrue(update > 0);

        Teacher teacher1 = teacherRecord.with("student").toObject();
        System.out.println(teacher1);
        // 老师(id=1)已经有没有学生
        Assert.assertNull(teacher1.getStudent());

        RecordList<Student, Long> students = studentModel.newQuery()
            .whereIn("id", "1", "2", "3", "4", "5")
            .get();
        teacherRecord.bind("students").attach(students);
        Teacher teacher2 = teacherRecord.toObject();
        Assert.assertNotNull(teacher2.getStudent());
    }

    @Test
    public void attach_HasOneOrMany_集合_单个() {
        Record<Teacher, Long> teacherRecord = teacherModel.findOrFail(1L);

        Teacher teacher1 = teacherRecord.with("students").toObject();
        System.out.println(teacher1);
        // 老师(id=1)已经有2个学生(id=5, id=6)
        Assert.assertEquals(teacher1.getStudents().size(), 2);

        // id=5的学生已经存在,所以应该Update执行没有影响
        Record<Student, Long> student = studentModel.findOrFail(5L);
        teacherRecord.bind("students").attach(student);

        Teacher teacher2 = teacherRecord.toObject();
        Assert.assertEquals(teacher2.getStudents().size(), 2);


        // id=1的学生不存在,所以应该Update执行
        Record<Student, Long> student1 = studentModel.findOrFail(1L);
        teacherRecord.bind("students").attach(student1);

        Teacher teacher3 = teacherRecord.toObject();
        Assert.assertEquals(teacher3.getStudents().size(), 3);
        System.out.println(teacher3);
    }

    @Test
    public void attach_HasOneOrMany_集合_多个() {
        Record<Teacher, Long> teacherRecord = teacherModel.findOrFail(1L);

        Teacher teacher1 = teacherRecord.with("students").toObject();
        System.out.println(teacher1);
        // 老师(id=1)已经有2个学生(id=5, id=6)
        Assert.assertEquals(teacher1.getStudents().size(), 2);

        RecordList<Student, Long> students = studentModel.newQuery()
            .whereIn("id", "1", "2", "3", "4", "5")
            .get();
        teacherRecord.bind("students").attach(students);

        Teacher teacher2 = teacherRecord.toObject();
        Assert.assertEquals(teacher2.getStudents().size(), 6);
        System.out.println(teacher2);
    }

    @Test
    public void attach_HasOneOrMany_集合_多个id() {
        Record<Teacher, Long> teacherRecord = teacherModel.findOrFail(1L);

        Teacher teacher1 = teacherRecord.with("students").toObject();
        System.out.println(teacher1);
        // 老师(id=1)已经有2个学生(id=5, id=6)
        Assert.assertEquals(teacher1.getStudents().size(), 2);

        teacherRecord.bind("students").attach(Arrays.asList("1", "2", "3", "4", "5"));

        Teacher teacher2 = teacherRecord.toObject();
        Assert.assertEquals(teacher2.getStudents().size(), 6);
        System.out.println(teacher2);
    }

    @Test
    public void detach_BelongsToMany_单个() {
        Record<Teacher, Long> teacherIntegerRecord = teacherModel.findOrFail(1L);

        Teacher studentsBelongsToMany = teacherIntegerRecord.with("studentsBelongsToMany").toObject();
        // 老师(id=1)已经有3个学生(id=1, id=2, id=3)
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().size(), 3);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(0).getId().intValue(), 1);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(1).getId().intValue(), 2);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(2).getId().intValue(), 3);

        Long oldCount = relationshipStudentTeacherModel.newQuery().count("*");

        // 解除与学生2的关系
        Record<Student, Long> student = studentModel.findOrFail(2L);
        teacherIntegerRecord.bind("studentsBelongsToMany").detach(student);

        Long new1Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(new1Count - oldCount, -1);

        // 再解除与学生2的关系
        teacherIntegerRecord.bind("studentsBelongsToMany").detach(student);

        Long new2Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(new2Count - new1Count, 0);
    }

    @Test
    public void detach_BelongsToMany_多个() {
        Record<Teacher, Long> teacherIntegerRecord = teacherModel.findOrFail(1L);

        Teacher studentsBelongsToMany = teacherIntegerRecord.with("studentsBelongsToMany").toObject();
        // 老师(id=1)已经有3个学生(id=1, id=2, id=3)
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().size(), 3);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(0).getId().intValue(), 1);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(1).getId().intValue(), 2);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(2).getId().intValue(), 3);

        Long oldCount = relationshipStudentTeacherModel.newQuery().count("*");

        // 解除与学生1,2,4,5的关系, 1,2应该解除, 4,5 无变化
        RecordList<Student, Long> records = studentModel.newQuery().whereIn("id", "1", "2", "4", "5").get();
        teacherIntegerRecord.bind("studentsBelongsToMany").detach(records);

        Long new1Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(new1Count - oldCount, -2);

        // 再解除与学生1,2,4,5的关系
        teacherIntegerRecord.bind("studentsBelongsToMany").detach(records);

        Long new2Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(new2Count - new1Count, 0);

        Assert.assertEquals(teacherIntegerRecord.toObject().getStudentsBelongsToMany().get(0).getId().intValue(), 3);
    }


    @Test
    public void detach_BelongsToMany_单个id() {
        Record<Teacher, Long> teacherIntegerRecord = teacherModel.findOrFail(1L);

        Teacher studentsBelongsToMany = teacherIntegerRecord.with("studentsBelongsToMany").toObject();
        // 老师(id=1)已经有3个学生(id=1, id=2, id=3)
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().size(), 3);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(0).getId().intValue(), 1);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(1).getId().intValue(), 2);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(2).getId().intValue(), 3);

        Long oldCount = relationshipStudentTeacherModel.newQuery().count("*");

        // 解除与学生2的关系
        teacherIntegerRecord.bind("studentsBelongsToMany").detach("2");

        Long new1Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(new1Count - oldCount, -1);

        // 再解除与学生2的关系
        teacherIntegerRecord.bind("studentsBelongsToMany").detach("2");

        Long new2Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(new2Count - new1Count, 0);
    }

    @Test
    public void detach_BelongsToMany_多个id() {
        Record<Teacher, Long> teacherIntegerRecord = teacherModel.findOrFail(1L);

        Teacher studentsBelongsToMany = teacherIntegerRecord.with("studentsBelongsToMany").toObject();
        // 老师(id=1)已经有3个学生(id=1, id=2, id=3)
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().size(), 3);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(0).getId().intValue(), 1);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(1).getId().intValue(), 2);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(2).getId().intValue(), 3);

        Long oldCount = relationshipStudentTeacherModel.newQuery().count("*");

        // 解除与学生1,2,4,5的关系, 1,2应该解除, 4,5 无变化
        teacherIntegerRecord.bind("studentsBelongsToMany").detach(Arrays.asList("1", "2", "4", "5"));

        Long new1Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(new1Count - oldCount, -2);

        // 再解除与学生1,2,4,5的关系
        teacherIntegerRecord.bind("studentsBelongsToMany").detach(Arrays.asList("1", "2", "4", "5"));

        Long new2Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(new2Count - new1Count, 0);

        Assert.assertEquals(teacherIntegerRecord.toObject().getStudentsBelongsToMany().get(0).getId().intValue(), 3);
    }

    @Test
    public void detach_BelongsToMany_ALL() {
        Record<Teacher, Long> teacherIntegerRecord = teacherModel.findOrFail(1L);

        Teacher studentsBelongsToMany = teacherIntegerRecord.with("studentsBelongsToMany").toObject();
        // 老师(id=1)已经有3个学生(id=1, id=2, id=3)
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().size(), 3);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(0).getId().intValue(), 1);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(1).getId().intValue(), 2);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(2).getId().intValue(), 3);

        Long oldCount = relationshipStudentTeacherModel.newQuery().count("*");

        teacherIntegerRecord.bind("studentsBelongsToMany").detach();

        Long new1Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(new1Count - oldCount, -3);

        // 再解除与学生1,2,4,5的关系
        teacherIntegerRecord.bind("studentsBelongsToMany").detach();

        Long new2Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(new2Count - new1Count, 0);

        System.out.println("-----------------");
        Assert.assertEquals(teacherIntegerRecord.toObject().getStudentsBelongsToMany().size(), 0);
    }

    @Test
    public void detach_HasOneOrMany_单体_单个() {
        Record<Teacher, Long> teacherRecord = teacherModel.findOrFail(1L);

        Teacher teacher1 = teacherRecord.with("student").toObject();
        System.out.println(teacher1);
        Assert.assertEquals(teacher1.getStudent().getId().intValue(), 5);

        Record<Student, Long> studentModelOrFail1 = studentModel.findOrFail(2L);
        teacherRecord.bind("student").detach(studentModelOrFail1);

        Teacher teacher11 = teacherRecord.toObject();
        Assert.assertNotNull(teacher11.getStudent());

        Record<Student, Long> studentModelOrFail = studentModel.findOrFail(5L);
        teacherRecord.bind("student").detach(studentModelOrFail);

        Teacher teacher = teacherRecord.toObject();
        Assert.assertNotEquals(teacher.getStudent().getId().intValue(), 5);
    }

    @Test
    public void detach_HasOneOrMany_单体_单个id() {
        Record<Teacher, Long> teacherRecord = teacherModel.findOrFail(1L);

        Teacher teacher1 = teacherRecord.with("student").toObject();
        System.out.println(teacher1);
        Assert.assertEquals(teacher1.getStudent().getId().intValue(), 5);

        teacherRecord.bind("student").detach("2");

        Teacher teacher11 = teacherRecord.toObject();
        Assert.assertNotNull(teacher11.getStudent());

        teacherRecord.bind("student").detach("5");

        Teacher teacher = teacherRecord.toObject();
        Assert.assertNotEquals(teacher.getStudent().getId().intValue(), 5);
    }

    @Test
    public void detach_HasOneOrMany_集合_单个() {
        Record<Teacher, Long> teacherRecord = teacherModel.findOrFail(1L);

        Teacher teacher1 = teacherRecord.with("students").toObject();
        System.out.println(teacher1);
        // 老师(id=1)已经有2个学生(id=5, id=6)
        Assert.assertEquals(teacher1.getStudents().size(), 2);

        Record<Student, Long> student = studentModel.findOrFail(2L);
        teacherRecord.bind("students").detach(student);

        Teacher teacher2 = teacherRecord.toObject();
        Assert.assertEquals(teacher2.getStudents().size(), 2);

        RecordList<Student, Long> records = studentModel.newQuery().whereIn("id", "5").get();
        teacherRecord.bind("students").detach(records);

        Teacher teacher3 = teacherRecord.toObject();
        Assert.assertEquals(teacher3.getStudents().size(), 1);
        System.out.println(teacher3);
    }

    @Test
    public void detach_HasOneOrMany_集合_多个() {
        Record<Teacher, Long> teacherRecord = teacherModel.findOrFail(1L);

        Teacher teacher1 = teacherRecord.with("students").toObject();
        System.out.println(teacher1);
        // 老师(id=1)已经有2个学生(id=5, id=6)
        Assert.assertEquals(teacher1.getStudents().size(), 2);

        Record<Student, Long> student = studentModel.findOrFail(2L);
        teacherRecord.bind("students").detach(student);

        Teacher teacher2 = teacherRecord.toObject();
        Assert.assertEquals(teacher2.getStudents().size(), 2);

        RecordList<Student, Long> records = studentModel.newQuery().whereIn("id", "5", "1", "3").get();
        teacherRecord.bind("students").detach(records);

        Teacher teacher3 = teacherRecord.toObject();
        Assert.assertEquals(teacher3.getStudents().size(), 1);
        System.out.println(teacher3);
    }

    @Test
    public void detach_HasOneOrMany_ALL() {
        Record<Teacher, Long> teacherRecord = teacherModel.findOrFail(1L);

        Teacher teacher1 = teacherRecord.with("students").toObject();
        System.out.println(teacher1);
        // 老师(id=1)已经有2个学生(id=5, id=6)
        Assert.assertEquals(teacher1.getStudents().size(), 2);

        teacherRecord.bind("students").detach();

        Teacher teacher2 = teacherRecord.toObject();
        Assert.assertEquals(teacher2.getStudents().size(), 0);
    }


    @Test
    public void detach_BelongsTo_单个() {
        Record<Student, Long> studentLongRecord = studentRelationModel.findOrFail(1L);
        Record<Teacher, Long> teacherIntegerRecord = teacherModel.findOrFail(1L);

        Student student = studentLongRecord.with("teacher").toObject();
        Assert.assertEquals(student.getTeacher().getId().intValue(), 6);
        studentLongRecord.bind("teacher").attach(teacherIntegerRecord);

        Student student1 = studentLongRecord.toObject();
        Assert.assertEquals(student1.getTeacher().getId().intValue(), 1);


        studentLongRecord.bind("teacher").detach(teacherIntegerRecord);

        Student student2 = studentLongRecord.toObject();
        Assert.assertNull(student2.getTeacher());
    }

    @Test
    public void detach_BelongsTo_多个() {
        Record<Student, Long> studentLongRecord = studentRelationModel.findOrFail(1L);
        Record<Teacher, Long> teacherIntegerRecord = teacherModel.findOrFail(1L);

        Student student = studentLongRecord.with("teacher").toObject();
        Assert.assertEquals(student.getTeacher().getId().intValue(), 6);
        studentLongRecord.bind("teacher").attach(teacherIntegerRecord);

        Student student1 = studentLongRecord.toObject();
        Assert.assertEquals(student1.getTeacher().getId().intValue(), 1);


        RecordList<Teacher, Long> all = teacherModel.findAll();
        studentLongRecord.bind("teacher").detach(all);

        Student student2 = studentLongRecord.toObject();
        Assert.assertNull(student2.getTeacher());
    }


    @Test
    public void detach_BelongsTo_单个id() {
        Record<Student, Long> studentLongRecord = studentRelationModel.findOrFail(1L);
        Record<Teacher, Long> teacherIntegerRecord = teacherModel.findOrFail(1L);

        Student student = studentLongRecord.with("teacher").toObject();
        Assert.assertEquals(student.getTeacher().getId().intValue(), 6);
        studentLongRecord.bind("teacher").attach("1");

        Student student1 = studentLongRecord.toObject();
        Assert.assertEquals(student1.getTeacher().getId().intValue(), 1);


        studentLongRecord.bind("teacher").detach("1");

        Student student2 = studentLongRecord.toObject();
        Assert.assertNull(student2.getTeacher());
    }

    @Test
    public void detach_BelongsTo_多个id() {
        Record<Student, Long> studentLongRecord = studentRelationModel.findOrFail(1L);
        Record<Teacher, Long> teacherIntegerRecord = teacherModel.findOrFail(1L);

        Student student = studentLongRecord.with("teacher").toObject();
        Assert.assertEquals(student.getTeacher().getId().intValue(), 6);
        studentLongRecord.bind("teacher").attach("1");

        Student student1 = studentLongRecord.toObject();
        Assert.assertEquals(student1.getTeacher().getId().intValue(), 1);

        studentLongRecord.bind("teacher").detach(Arrays.asList("1", "2", "4444"));

        Student student2 = studentLongRecord.toObject();
        Assert.assertNull(student2.getTeacher());
    }

    @Test
    public void detach_BelongsTo_ALL() {
        Record<Student, Long> studentLongRecord = studentRelationModel.findOrFail(1L);
        Record<Teacher, Long> teacherIntegerRecord = teacherModel.findOrFail(1L);

        Student student = studentLongRecord.with("teacher").toObject();
        Assert.assertEquals(student.getTeacher().getId().intValue(), 6);

        studentLongRecord.bind("teacher").detach();

        Student student2 = studentLongRecord.toObject();
        Assert.assertNull(student2.getTeacher());
    }

    @Test
    public void sync_BelongsToMany_单个_and_多个() {
        Record<Teacher, Long> teacherIntegerRecord = teacherModel.findOrFail(1L);

        Teacher studentsBelongsToMany = teacherIntegerRecord.with("studentsBelongsToMany").toObject();
        // 老师(id=1)已经有3个学生(id=1, id=2, id=3)
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().size(), 3);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(0).getId().intValue(), 1);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(1).getId().intValue(), 2);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(2).getId().intValue(), 3);

        Long oldCount = relationshipStudentTeacherModel.newQuery().count("*");

        // 学生2的关系
        Record<Student, Long> student = studentModel.findOrFail(2L);
        teacherIntegerRecord.bind("studentsBelongsToMany").sync(student);

        Long new1Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(new1Count - oldCount, -2);

        // 学生1,2,3,4的关系
        RecordList<Student, Long> records = studentModel.newQuery().whereIn("id", "1", "2", "3", "4").get();
        teacherIntegerRecord.bind("studentsBelongsToMany").sync(records);

        Long new2Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(new2Count - new1Count, 3);

        Teacher teacher = teacherIntegerRecord.toObject();
        Assert.assertEquals(teacher.getStudentsBelongsToMany().size(), 4);
        System.out.println(teacher);
    }

    @Test
    public void sync_BelongsToMany_单个_and_多个_附带信息到中间表() {
        String note = "ssssss";
        Record<Teacher, Long> teacherIntegerRecord = teacherModel.findOrFail(1L);

        Teacher studentsBelongsToMany = teacherIntegerRecord
            .with("studentsBelongsToMany")
            .with("relationshipStudentTeachers")
            .toObject();
        // 老师(id=1)已经有3个学生(id=1, id=2, id=3)
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().size(), 3);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(0).getId().intValue(), 1);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(1).getId().intValue(), 2);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(2).getId().intValue(), 3);

        Long oldCount = relationshipStudentTeacherModel.newQuery().count("*");

        // 学生2的关系
        Record<Student, Long> student = studentModel.findOrFail(2L);
        Map<String, Object> map = new HashMap<>();
        map.put("note", note);
        int studentsBelongsToMany2 = teacherIntegerRecord.bind("studentsBelongsToMany").sync(student, map);

        Long new1Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(studentsBelongsToMany2, 2);
        Assert.assertEquals(new1Count - oldCount, -2);

        // 学生1,2,3,4的关系
        RecordList<Student, Long> records =
            studentModel.newQuery().whereIn("id", "1", "2", "3", "4").get();
        int studentsBelongsToMany1 =
            teacherIntegerRecord.bind("studentsBelongsToMany").sync(records, map);
        Assert.assertEquals(studentsBelongsToMany1, 3);

        Long new2Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(new2Count - new1Count, 3);

        Teacher teacher = teacherIntegerRecord.toObject();
        System.out.println(teacher);
        Assert.assertEquals(teacher.getStudentsBelongsToMany().size(), 4);

        for (RelationshipStudentTeacher relationshipStudentTeacher : teacher.getRelationshipStudentTeachers()) {
            if (relationshipStudentTeacher.getStudentId().intValue() != 2) {
                Assert.assertEquals(relationshipStudentTeacher.getNote(), note);
            }
        }
    }

    @Test
    public void sync_BelongsToMany_单个_and_多个ID() {
        Record<Teacher, Long> teacherIntegerRecord = teacherModel.findOrFail(1L);

        Teacher studentsBelongsToMany = teacherIntegerRecord.with("studentsBelongsToMany").toObject();
        // 老师(id=1)已经有3个学生(id=1, id=2, id=3)
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().size(), 3);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(0).getId().intValue(), 1);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(1).getId().intValue(), 2);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(2).getId().intValue(), 3);

        Long oldCount = relationshipStudentTeacherModel.newQuery().count("*");

        // 学生2的关系
        teacherIntegerRecord.bind("studentsBelongsToMany").sync(2L);

        Long new1Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(new1Count - oldCount, -2);

        // 学生1,2,3,4的关系
        teacherIntegerRecord.bind("studentsBelongsToMany").sync(Arrays.asList(1L, 2L, 3L, 4L));

        Long new2Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(new2Count - new1Count, 3);

        Teacher teacher = teacherIntegerRecord.toObject();
        Assert.assertEquals(teacher.getStudentsBelongsToMany().size(), 4);
        System.out.println(teacher);
    }

    @Test
    public void sync_BelongsToMany_单个_and_多个ID_附带信息到中间表() {
        String note = "ssssss";
        Record<Teacher, Long> teacherIntegerRecord = teacherModel.findOrFail(1L);

        Teacher studentsBelongsToMany = teacherIntegerRecord
            .with("studentsBelongsToMany")
            .with("relationshipStudentTeachers")
            .toObject();
        // 老师(id=1)已经有3个学生(id=1, id=2, id=3)
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().size(), 3);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(0).getId().intValue(), 1);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(1).getId().intValue(), 2);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(2).getId().intValue(), 3);

        Long oldCount = relationshipStudentTeacherModel.newQuery().count("*");

        // 学生2的关系
        Map<String, Object> map = new HashMap<>();
        map.put("note", note);
        int studentsBelongsToMany2 = teacherIntegerRecord.bind("studentsBelongsToMany").sync(2L, map);

        Long new1Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(studentsBelongsToMany2, 2);
        Assert.assertEquals(new1Count - oldCount, -2);

        // 学生1,2,3,4的关系
        int studentsBelongsToMany1 =
            teacherIntegerRecord.bind("studentsBelongsToMany").sync(Arrays.asList(1L, 2L, 3L, 4L), map);
        Assert.assertEquals(studentsBelongsToMany1, 3);

        Long new2Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(new2Count - new1Count, 3);

        Teacher teacher = teacherIntegerRecord.toObject();
        System.out.println(teacher);

        Assert.assertEquals(teacher.getStudentsBelongsToMany().size(), 4);

        for (RelationshipStudentTeacher relationshipStudentTeacher : teacher.getRelationshipStudentTeachers()) {
            if (relationshipStudentTeacher.getStudentId().intValue() != 2) {
                Assert.assertEquals(relationshipStudentTeacher.getNote(), note);
            }
        }
    }

    @Test
    public void sync_HasOneOrMany_单体_单个() {
        Record<Teacher, Long> teacherRecord = teacherModel.findOrFail(1L);

        teacherRecord.with("student").toObject();

        Record<Student, Long> student = studentModel.findOrFail(5L);
        teacherRecord.bind("student").sync(student);

        Teacher teacher2 = teacherRecord.toObject();
        Assert.assertEquals(teacher2.getStudent().getId().intValue(), 5);

        Long teacher_id = studentModel.newQuery().where("teacher_id", "1").count("*");
        Assert.assertEquals(teacher_id.intValue(), 1);
    }

    @Test
    public void sync_HasOneOrMany_单体_多个() {
        Record<Teacher, Long> teacherRecord = teacherModel.findOrFail(1L);

        Teacher teacher1 = teacherRecord.with("student").toObject();

        RecordList<Student, Long> students = studentModel.newQuery()
            .whereIn("id", "1", "2", "3", "4", "5")
            .get();
        teacherRecord.bind("students").sync(students);
        Teacher teacher2 = teacherRecord.toObject();
        Assert.assertNotNull(teacher2.getStudent());
    }

    @Test
    public void sync_HasOneOrMany_集合_单个() {
        Record<Teacher, Long> teacherRecord = teacherModel.findOrFail(1L);

        Teacher teacher1 = teacherRecord.with("students").toObject();
        System.out.println(teacher1);
        // 老师(id=1)已经有2个学生(id=5, id=6)
        Assert.assertEquals(teacher1.getStudents().size(), 2);

        Record<Student, Long> student = studentModel.findOrFail(5L);
        int students = teacherRecord.bind("students").sync(student);
        Assert.assertEquals(students, 1);

        Teacher teacher2 = teacherRecord.toObject();
        Assert.assertEquals(teacher2.getStudents().size(), 1);

        Record<Student, Long> student1 = studentModel.findOrFail(1L);
        int students1 = teacherRecord.bind("students").sync(student1);
        Assert.assertEquals(students1, 2);

        Teacher teacher3 = teacherRecord.toObject();
        Assert.assertEquals(teacher3.getStudents().size(), 1);
        System.out.println(teacher3);
    }

    @Test
    public void sync_HasOneOrMany_集合_多个() {
        Record<Teacher, Long> teacherRecord = teacherModel.findOrFail(1L);

        Teacher teacher1 = teacherRecord.with("students").toObject();
        System.out.println(teacher1);
        // 老师(id=1)已经有2个学生(id=5, id=6)
        Assert.assertEquals(teacher1.getStudents().size(), 2);

        RecordList<Student, Long> students = studentModel.newQuery()
            .whereIn("id", "1", "2", "3", "4", "5")
            .get();
        int students1 = teacherRecord.bind("students").sync(students);
        // 插入 1,2,3,4 解除 6
        Assert.assertEquals(students1, 5);


        Teacher teacher2 = teacherRecord.toObject();
        Assert.assertEquals(teacher2.getStudents().size(), 5);
        System.out.println(teacher2);
    }

    @Test
    public void sync_HasOneOrMany_集合_多个id() {
        Record<Teacher, Long> teacherRecord = teacherModel.findOrFail(1L);

        Teacher teacher1 = teacherRecord.with("students").toObject();
        System.out.println(teacher1);
        // 老师(id=1)已经有2个学生(id=5, id=6)
        Assert.assertEquals(teacher1.getStudents().size(), 2);

        int students = teacherRecord.bind("students").sync(Arrays.asList("1", "2", "3", "4", "5"));
        // 插入 1,2,3,4 解除 6
        Assert.assertEquals(students, 5);

        Teacher teacher2 = teacherRecord.toObject();
        Assert.assertEquals(teacher2.getStudents().size(), 5);
        System.out.println(teacher2);
    }

    @Test
    public void sync_BelongsTo_单个() {
        Record<Student, Long> studentLongRecord = studentRelationModel.findOrFail(1L);
        Record<Teacher, Long> teacherIntegerRecord = teacherModel.findOrFail(1L);
        Student student = studentLongRecord.with("teacher").toObject();
        Assert.assertEquals(student.getTeacher().getId().intValue(), 6);
        studentLongRecord.bind("teacher").sync(teacherIntegerRecord);
        Student student1 = studentLongRecord.toObject();
        Assert.assertEquals(student1.getTeacher().getId().intValue(), 1);
    }

    @Test
    public void sync_BelongsTo_单个id() {
        Record<Student, Long> studentLongRecord = studentRelationModel.findOrFail(1L);
        Student student = studentLongRecord.with("teacher").toObject();
        Assert.assertEquals(student.getTeacher().getId().intValue(), 6);
        studentLongRecord.bind("teacher").sync("1");
        Student student1 = studentLongRecord.toObject();
        Assert.assertEquals(student1.getTeacher().getId().intValue(), 1);
    }

    @Test
    public void sync_BelongsTo_异常() {
        Record<Student, Long> studentLongRecord = studentRelationModel.findOrFail(1L);
        RecordList<Teacher, Long> all = teacherModel.findAll();
        Student student = studentLongRecord.with("teacher").toObject();
        Assert.assertThrows(RelationAttachException.class, () -> {
            studentLongRecord.bind("teacher").sync(all);
        });
    }


    @Test
    public void toggle_BelongsToMany_单个_and_多个() {
        Record<Teacher, Long> teacherIntegerRecord = teacherModel.findOrFail(1L);

        Teacher studentsBelongsToMany = teacherIntegerRecord.with("studentsBelongsToMany").toObject();
        // 老师(id=1)已经有3个学生(id=1, id=2, id=3)
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().size(), 3);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(0).getId().intValue(), 1);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(1).getId().intValue(), 2);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(2).getId().intValue(), 3);

        Long oldCount = relationshipStudentTeacherModel.newQuery().count("*");

        // 学生2的关系
        Record<Student, Long> student = studentModel.findOrFail(2L);
        // 反转2的关系, 即解除2的关系
        // 剩余 1,3
        teacherIntegerRecord.bind("studentsBelongsToMany").toggle(student);

        Long new1Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(new1Count - oldCount, -1);

        RecordList<Student, Long> records = studentModel.newQuery().whereIn("id", "1", "2", "3", "4").get();
        // 反转 1,2,3,4 的关系
        // 剩余 2,4
        teacherIntegerRecord.bind("studentsBelongsToMany").toggle(records);

        Long new2Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(new2Count - new1Count, 0);

        Teacher teacher = teacherIntegerRecord.toObject();
        Assert.assertEquals(teacher.getStudentsBelongsToMany().size(), 2);
        System.out.println(teacher);
    }

    @Test
    public void toggle_BelongsToMany_单个_and_多个_附带信息到中间表() {
        String note = "ssssss";
        Record<Teacher, Long> teacherIntegerRecord = teacherModel.findOrFail(1L);

        Teacher studentsBelongsToMany = teacherIntegerRecord
            .with("studentsBelongsToMany")
            .with("relationshipStudentTeachers")
            .toObject();
        // 老师(id=1)已经有3个学生(id=1, id=2, id=3)
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().size(), 3);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(0).getId().intValue(), 1);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(1).getId().intValue(), 2);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(2).getId().intValue(), 3);

        Long oldCount = relationshipStudentTeacherModel.newQuery().count("*");

        // 学生2的关系
        Record<Student, Long> student = studentModel.findOrFail(2L);
        Map<String, Object> map = new HashMap<>();
        map.put("note", note);
        int studentsBelongsToMany2 = teacherIntegerRecord.bind("studentsBelongsToMany").toggle(student, map);

        Long new1Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(studentsBelongsToMany2, 1);
        Assert.assertEquals(new1Count - oldCount, -1);

        // 学生1,2,3,4的关系
        RecordList<Student, Long> records =
            studentModel.newQuery().whereIn("id", "1", "2", "3", "4").get();
        int studentsBelongsToMany1 =
            teacherIntegerRecord.bind("studentsBelongsToMany").toggle(records, map);
        Assert.assertEquals(studentsBelongsToMany1, 4);

        Long new2Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(new2Count - new1Count, 0);

        Teacher teacher = teacherIntegerRecord.toObject();
        System.out.println(teacher);
        Assert.assertEquals(teacher.getStudentsBelongsToMany().size(), 2);

        for (RelationshipStudentTeacher relationshipStudentTeacher : teacher.getRelationshipStudentTeachers()) {
            Assert.assertEquals(relationshipStudentTeacher.getNote(), note);
        }
    }

    @Test
    public void toggle_BelongsToMany_单个_and_多个ID() {
        Record<Teacher, Long> teacherIntegerRecord = teacherModel.findOrFail(1L);

        Teacher studentsBelongsToMany = teacherIntegerRecord.with("studentsBelongsToMany").toObject();
        // 老师(id=1)已经有3个学生(id=1, id=2, id=3)
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().size(), 3);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(0).getId().intValue(), 1);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(1).getId().intValue(), 2);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(2).getId().intValue(), 3);

        Long oldCount = relationshipStudentTeacherModel.newQuery().count("*");

        // 学生2的关系
        teacherIntegerRecord.bind("studentsBelongsToMany").toggle(2L);

        Long new1Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(new1Count - oldCount, -1);

        // 学生1,2,3,4的关系
        teacherIntegerRecord.bind("studentsBelongsToMany").toggle(Arrays.asList(1L, 2L, 3L, 4L));

        Long new2Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(new2Count - new1Count, 0);

        Teacher teacher = teacherIntegerRecord.toObject();
        Assert.assertEquals(teacher.getStudentsBelongsToMany().size(), 2);
        System.out.println(teacher);
    }

    @Test
    public void toggle_BelongsToMany_单个_and_多个ID_附带信息到中间表() {
        String note = "ssssss";
        Record<Teacher, Long> teacherIntegerRecord = teacherModel.findOrFail(1L);

        Teacher studentsBelongsToMany = teacherIntegerRecord
            .with("studentsBelongsToMany")
            .with("relationshipStudentTeachers")
            .toObject();
        // 老师(id=1)已经有3个学生(id=1, id=2, id=3)
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().size(), 3);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(0).getId().intValue(), 1);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(1).getId().intValue(), 2);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(2).getId().intValue(), 3);

        Long oldCount = relationshipStudentTeacherModel.newQuery().count("*");

        // 学生2的关系
        Map<String, Object> map = new HashMap<>();
        map.put("note", note);
        // 切换2, 即是 解除2, 剩余1,3
        int studentsBelongsToMany2 = teacherIntegerRecord.bind("studentsBelongsToMany").toggle(2L, map);

        Long new1Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(studentsBelongsToMany2, 1);
        Assert.assertEquals(new1Count - oldCount, -1);

        // 学生1,2,3,4的关系
        int studentsBelongsToMany1 =
            teacherIntegerRecord.bind("studentsBelongsToMany").toggle(Arrays.asList(1L, 2L, 3L, 4L), map);
        Assert.assertEquals(studentsBelongsToMany1, 4);

        Long new2Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(new2Count - new1Count, 0);

        Teacher teacher = teacherIntegerRecord.toObject();
        System.out.println(teacher);

        Assert.assertEquals(teacher.getStudentsBelongsToMany().size(), 2);

        for (RelationshipStudentTeacher relationshipStudentTeacher : teacher.getRelationshipStudentTeachers()) {
            Assert.assertEquals(relationshipStudentTeacher.getNote(), note);
        }
    }

    @Test
    public void toggle_BelongsToMany_单个_and_多个ID_附带信息到中间表_String传参() {
        String note = "ssssss";
        Record<Teacher, Long> teacherIntegerRecord = teacherModel.findOrFail(1L);

        Teacher studentsBelongsToMany = teacherIntegerRecord
            .with("studentsBelongsToMany")
            .with("relationshipStudentTeachers")
            .toObject();
        // 老师(id=1)已经有3个学生(id=1, id=2, id=3)
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().size(), 3);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(0).getId().intValue(), 1);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(1).getId().intValue(), 2);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(2).getId().intValue(), 3);

        Long oldCount = relationshipStudentTeacherModel.newQuery().count("*");

        // 学生2的关系
        Map<String, Object> map = new HashMap<>();
        map.put("note", note);
        // 切换2, 即是 解除2, 剩余1,3
        int studentsBelongsToMany2 = teacherIntegerRecord.bind("studentsBelongsToMany").toggle("2", map);

        Long new1Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(studentsBelongsToMany2, 1);
        Assert.assertEquals(new1Count - oldCount, -1);

        // 学生1,2,3,4的关系
        int studentsBelongsToMany1 =
            teacherIntegerRecord.bind("studentsBelongsToMany").toggle(Arrays.asList("1", "2", "3", "4"), map);
        Assert.assertEquals(studentsBelongsToMany1, 4);

        Long new2Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(new2Count - new1Count, 0);

        Teacher teacher = teacherIntegerRecord.toObject();
        System.out.println(teacher);

        Assert.assertEquals(teacher.getStudentsBelongsToMany().size(), 2);

        for (RelationshipStudentTeacher relationshipStudentTeacher : teacher.getRelationshipStudentTeachers()) {
            Assert.assertEquals(relationshipStudentTeacher.getNote(), note);
        }
    }

    @Test
    public void toggle_HasOneOrMany_单体_单个() {
        Record<Teacher, Long> teacherRecord = teacherModel.findOrFail(1L);
        // 老师(id=1)已经有2个学生(id=5, id=6)

        teacherRecord.with("student").toObject();

        Record<Student, Long> student = studentModel.findOrFail(5L);
        teacherRecord.bind("student").toggle(student);

        Teacher teacher2 = teacherRecord.toObject();
        Assert.assertEquals(teacher2.getStudent().getId().intValue(), 6);

        Long teacher_id = studentModel.newQuery().where("teacher_id", "1").count("*");
        Assert.assertEquals(teacher_id.intValue(), 1);
    }

    @Test
    public void toggle_HasOneOrMany_单体_多个() {
        Record<Teacher, Long> teacherRecord = teacherModel.findOrFail(1L);
        // 老师(id=1)已经有2个学生(id=5, id=6)

        Teacher teacher1 = teacherRecord.with("student").toObject();

        RecordList<Student, Long> students = studentModel.newQuery()
            .whereIn("id", "1", "2", "3", "4", "5")
            .get();
        // 切换
        teacherRecord.bind("students").toggle(students);
        Teacher teacher2 = teacherRecord.toObject();
        Assert.assertNotNull(teacher2.getStudent());
    }

    @Test
    public void toggle_HasOneOrMany_集合_单个() {
        Record<Teacher, Long> teacherRecord = teacherModel.findOrFail(1L);

        Teacher teacher1 = teacherRecord.with("students").toObject();
        System.out.println(teacher1);
        // 老师(id=1)已经有2个学生(id=5, id=6)
        Assert.assertEquals(teacher1.getStudents().size(), 2);

        Record<Student, Long> student = studentModel.findOrFail(5L);
        int students = teacherRecord.bind("students").toggle(student);
        Assert.assertEquals(students, 1);

        Teacher teacher2 = teacherRecord.toObject();
        Assert.assertEquals(teacher2.getStudents().size(), 1);

        Record<Student, Long> student1 = studentModel.findOrFail(1L);
        int students1 = teacherRecord.bind("students").toggle(student1);
        Assert.assertEquals(students1, 1);

        Teacher teacher3 = teacherRecord.toObject();
        Assert.assertEquals(teacher3.getStudents().size(), 2);
        System.out.println(teacher3);
    }

    @Test
    public void toggle_HasOneOrMany_集合_多个() {
        Record<Teacher, Long> teacherRecord = teacherModel.findOrFail(1L);

        Teacher teacher1 = teacherRecord.with("students", builder -> builder.orderBy("id", OrderBy.ASC)).toObject();
        System.out.println(teacher1);
        // 老师(id=1)已经有2个学生(id=5, id=6)
        Assert.assertEquals(teacher1.getStudents().size(), 2);

        RecordList<Student, Long> students = studentModel.newQuery()
            .whereIn("id", "1", "2", "3", "4", "5")
            .get();
        int students1 = teacherRecord.bind("students").toggle(students);
        // 插入 1,2,3,4 解除 5
        Assert.assertEquals(students1, 5);

        Teacher teacher2 = teacherRecord.toObject();
        System.out.println(teacher2);
        Assert.assertEquals(teacher2.getStudents().size(), 5);
        Assert.assertEquals(teacher2.getStudents().get(0).getId().intValue(), 1);
        Assert.assertEquals(teacher2.getStudents().get(1).getId().intValue(), 2);
        Assert.assertEquals(teacher2.getStudents().get(2).getId().intValue(), 3);
        Assert.assertEquals(teacher2.getStudents().get(3).getId().intValue(), 4);
        Assert.assertEquals(teacher2.getStudents().get(4).getId().intValue(), 6);
    }

    @Test
    public void toggle_HasOneOrMany_集合_多个id() {
        Record<Teacher, Long> teacherRecord = teacherModel.findOrFail(1L);

        Teacher teacher1 = teacherRecord.with("students", builder -> builder.orderBy("id", OrderBy.ASC)).toObject();
        System.out.println(teacher1);
        // 老师(id=1)已经有2个学生(id=5, id=6)
        Assert.assertEquals(teacher1.getStudents().size(), 2);

        int students = teacherRecord.bind("students").toggle(Arrays.asList(1L, 2L, 3L, 4L, 5L));
        // 插入 1,2,3,4 解除 6
        Assert.assertEquals(students, 5);

        Teacher teacher2 = teacherRecord.toObject();
        System.out.println(teacher2);
        Assert.assertEquals(teacher2.getStudents().size(), 5);
        Assert.assertEquals(teacher2.getStudents().get(0).getId().intValue(), 1);
        Assert.assertEquals(teacher2.getStudents().get(1).getId().intValue(), 2);
        Assert.assertEquals(teacher2.getStudents().get(2).getId().intValue(), 3);
        Assert.assertEquals(teacher2.getStudents().get(3).getId().intValue(), 4);
        Assert.assertEquals(teacher2.getStudents().get(4).getId().intValue(), 6);
    }

    @Test
    public void toggle_BelongsTo_单个() {
        Record<Student, Long> studentLongRecord = studentRelationModel.findOrFail(1L);
        Record<Teacher, Long> teacherIntegerRecord = teacherModel.findOrFail(1L);
        Student student = studentLongRecord.with("teacher").toObject();
        Assert.assertEquals(student.getTeacher().getId().intValue(), 6);
        studentLongRecord.bind("teacher").toggle(teacherIntegerRecord);
        Student student1 = studentLongRecord.toObject();
        Assert.assertEquals(student1.getTeacher().getId().intValue(), 1);
    }

    @Test
    public void toggle_BelongsTo_单个id() {
        Record<Student, Long> studentLongRecord = studentRelationModel.findOrFail(1L);
        Student student = studentLongRecord.with("teacher").toObject();
        Assert.assertEquals(student.getTeacher().getId().intValue(), 6);
        studentLongRecord.bind("teacher").toggle("1");
        Student student1 = studentLongRecord.toObject();
        Assert.assertEquals(student1.getTeacher().getId().intValue(), 1);
    }

    @Test
    public void toggle_BelongsTo_异常() {
        Record<Student, Long> studentLongRecord = studentRelationModel.findOrFail(1L);
        RecordList<Teacher, Long> all = teacherModel.findAll();
        Student student = studentLongRecord.with("teacher").toObject();
        Assert.assertThrows(RelationAttachException.class, () -> {
            studentLongRecord.bind("teacher").toggle(all);
        });

        int num1 = studentLongRecord.bind("teacher").attach(new ArrayList<>());
        Assert.assertEquals(num1, 0);
        int num2 = studentLongRecord.bind("teacher").detach(new ArrayList<>());
        Assert.assertEquals(num2, 0);
        int num3 = studentLongRecord.bind("teacher").toggle(new ArrayList<>());
        Assert.assertEquals(num3, 0);
    }

    @Test
    public void 检查属性变化_isDirty_isClean() {
        Teacher teacher = new Teacher();
        teacher.setName("新老师");
        teacher.setAge(44);
        teacher.setSex(2);
        Record<Teacher, Long> record = teacherModel.create(teacher);

        Teacher teacher1 = record.getEntity();

        Assert.assertFalse(record.isDirty());
        Assert.assertFalse(record.isDirty(Teacher::getAge));
        Assert.assertFalse(record.isDirty(Teacher::getName));
        Assert.assertFalse(record.isDirty(Teacher::getAge, Teacher::getName));

        teacher1.setName("新老师的新名字");

        Assert.assertTrue(record.isDirty());
        Assert.assertFalse(record.isDirty(Teacher::getAge));
        Assert.assertTrue(record.isDirty(Teacher::getName));
        Assert.assertTrue(record.isDirty(Teacher::getAge, Teacher::getName));

        Assert.assertFalse(record.isClean());
        Assert.assertTrue(record.isClean(Teacher::getAge));
        Assert.assertFalse(record.isClean(Teacher::getName));
        Assert.assertFalse(record.isClean(Teacher::getAge, Teacher::getName));

        record.save();

        Assert.assertFalse(record.isDirty());
        Assert.assertTrue(record.isClean());
    }

    @Test
    public void 检查属性变化_wasChanged() {
        Teacher teacher = new Teacher();
        teacher.setName("新老师");
        teacher.setAge(44);
        teacher.setSex(2);
        Record<Teacher, Long> record = teacherModel.create(teacher);

        Teacher teacher1 = record.getEntity();

        Assert.assertFalse(record.wasChanged());
        Assert.assertFalse(record.wasChanged(Teacher::getAge));
        Assert.assertFalse(record.wasChanged(Teacher::getName));
        Assert.assertFalse(record.wasChanged(Teacher::getAge, Teacher::getName));

        // 仅设置, 未提交
        teacher1.setName("新老师的新名字");

        Assert.assertFalse(record.wasChanged());
        Assert.assertFalse(record.wasChanged(Teacher::getAge));
        Assert.assertFalse(record.wasChanged(Teacher::getName));
        Assert.assertFalse(record.wasChanged(Teacher::getAge, Teacher::getName));

        // 提交到数据库
        record.save();

        Assert.assertTrue(record.wasChanged());
        Assert.assertFalse(record.wasChanged(Teacher::getAge));
        Assert.assertTrue(record.wasChanged(Teacher::getName));
        Assert.assertTrue(record.wasChanged(Teacher::getAge, Teacher::getName));
    }

    @Test
    public void 检查属性变化_getOriginal() {
        // 查询 sql
        Record<Teacher, Long> teacherRecord = teacherModel.findOrFail(1);
        Teacher teacher = teacherRecord.getEntity();

        Assert.assertEquals("张淑明", teacher.getName());
        Assert.assertEquals(22, teacher.getAge().intValue());

        // 仅设置, 未提交
        teacher.setName("小明");

        Assert.assertEquals("小明", teacher.getName());
        // 原始值
        Assert.assertEquals("张淑明", teacherRecord.getOriginal(Teacher::getName));

        // 提交到数据库 (更新 sql)
        teacherRecord.save();
        // 原始值
        Assert.assertEquals("张淑明", teacherRecord.getOriginal(Teacher::getName));
        // 当前实际值
        Assert.assertEquals("小明", teacherRecord.toObject().getName());

        // 数据库重新获取 (查询 sql)
        teacherRecord.refresh();
        // 原始值, 将变化为最近次获取的值
        Assert.assertEquals("小明", teacherRecord.getOriginal(Teacher::getName));

    }


}
