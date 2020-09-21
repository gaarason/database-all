package gaarason.database.test;

import gaarason.database.connection.GaarasonDataSourceWrapper;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.eloquent.appointment.OrderBy;
import gaarason.database.exception.RelationAttachException;
import gaarason.database.test.models.StudentModel;
import gaarason.database.test.models.StudentORMModel;
import gaarason.database.test.parent.BaseTests;
import gaarason.database.test.relation.data.model.RelationshipStudentTeacherModel;
import gaarason.database.test.relation.data.model.TeacherModel;
import gaarason.database.test.relation.data.pojo.RelationshipStudentTeacher;
import gaarason.database.test.relation.data.pojo.Student;
import gaarason.database.test.relation.data.pojo.Teacher;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.CountDownLatch;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
public class ORMTests extends BaseTests {

    private static StudentORMModel studentORMModel = new StudentORMModel();

    private static StudentModel studentModel = new StudentModel();

    private static TeacherModel teacherModel = new TeacherModel();

    private static gaarason.database.test.relation.data.model.StudentModel studentRelationModel =
            new gaarason.database.test.relation.data.model.StudentModel();

    private static RelationshipStudentTeacherModel relationshipStudentTeacherModel = new RelationshipStudentTeacherModel();

    protected List<DataSource> getDataSourceList() {
        GaarasonDataSourceWrapper gaarasonDataSourceWrapper = studentORMModel.getGaarasonDataSource();
        return gaarasonDataSourceWrapper.getMasterDataSourceList();
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
    public void attach_BelongsToMany_单个() {
        Record<Teacher, Integer> teacherIntegerRecord = teacherModel.findOrFail(1);

        Teacher studentsBelongsToMany = teacherIntegerRecord.with("studentsBelongsToMany").toObject();
        // 老师(id=1)已经有3个学生(id=1, id=2, id=3)
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().size(), 3);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(0).getId().intValue(), 1);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(1).getId().intValue(), 2);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(2).getId().intValue(), 3);

        Long oldCount = relationshipStudentTeacherModel.newQuery().count("*");

        Record<StudentModel.Entity, Integer> student = studentModel.findOrFail(2);
        teacherIntegerRecord.bind("studentsBelongsToMany").attach(student);

        Long new1Count = relationshipStudentTeacherModel.newQuery().count("*");

        // 因为, 老师(id=1)已经有3个学生(id=1, id=2, id=3), 所以增加学生(id=2)不会产生任何操作
        Assert.assertEquals(new1Count - oldCount, 0);

        Record<StudentModel.Entity, Integer> student2 = studentModel.findOrFail(4);
        teacherIntegerRecord.bind("studentsBelongsToMany").attach(student2);
        Long new2Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(new2Count - oldCount, 1);

        Teacher teacher = teacherIntegerRecord.toObject();
        Assert.assertEquals(teacher.getStudentsBelongsToMany().size(), 4);
    }

    @Test
    public void attach_BelongsToMany_单个_附带信息到中间表() {
        String                   note                 = "ssssss";
        Record<Teacher, Integer> teacherIntegerRecord = teacherModel.findOrFail(1);

        Teacher studentsBelongsToMany = teacherIntegerRecord.with("studentsBelongsToMany")
                .with("relationshipStudentTeachers")
                .toObject();
        // 老师(id=1)已经有3个学生(id=1, id=2, id=3)
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().size(), 3);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(0).getId().intValue(), 1);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(1).getId().intValue(), 2);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(2).getId().intValue(), 3);

        Long oldCount = relationshipStudentTeacherModel.newQuery().count("*");

        Record<StudentModel.Entity, Integer> student = studentModel.findOrFail(2);
        teacherIntegerRecord.bind("studentsBelongsToMany").attach(student);

        Long new1Count = relationshipStudentTeacherModel.newQuery().count("*");

        // 因为, 老师(id=1)已经有3个学生(id=1, id=2, id=3), 所以增加学生(id=2)不会产生任何操作
        Assert.assertEquals(new1Count - oldCount, 0);

        Record<StudentModel.Entity, Integer> student2 = studentModel.findOrFail(4);

        HashMap<String, String> map = new HashMap<>();
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
        String                   note                 = "ssssss";
        Record<Teacher, Integer> teacherIntegerRecord = teacherModel.findOrFail(1);

        Teacher studentsBelongsToMany = teacherIntegerRecord.with("studentsBelongsToMany")
                .with("relationshipStudentTeachers")
                .toObject();
        // 老师(id=1)已经有3个学生(id=1, id=2, id=3)
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().size(), 3);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(0).getId().intValue(), 1);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(1).getId().intValue(), 2);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(2).getId().intValue(), 3);

        Long oldCount = relationshipStudentTeacherModel.newQuery().count("*");

        RecordList<StudentModel.Entity, Integer> student =
                studentModel.newQuery().whereIn("id", "1", "2", "3", "4", "5").get();
        Assert.assertEquals(student.size(), 5);


        HashMap<String, String> map = new HashMap<>();
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
        String                   note                 = "ssssss";
        Record<Teacher, Integer> teacherIntegerRecord = teacherModel.findOrFail(1);

        Teacher studentsBelongsToMany = teacherIntegerRecord.with("studentsBelongsToMany")
                .with("relationshipStudentTeachers")
                .toObject();
        // 老师(id=1)已经有3个学生(id=1, id=2, id=3)
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().size(), 3);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(0).getId().intValue(), 1);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(1).getId().intValue(), 2);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(2).getId().intValue(), 3);

        Long oldCount = relationshipStudentTeacherModel.newQuery().count("*");

        teacherIntegerRecord.bind("studentsBelongsToMany").attach(Arrays.asList("1", "2", "3", "4", "5"));

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
        String                   note                 = "ssssss";
        Record<Teacher, Integer> teacherIntegerRecord = teacherModel.findOrFail(1);

        Teacher studentsBelongsToMany = teacherIntegerRecord.with("studentsBelongsToMany")
                .with("relationshipStudentTeachers")
                .toObject();
        // 老师(id=1)已经有3个学生(id=1, id=2, id=3)
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().size(), 3);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(0).getId().intValue(), 1);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(1).getId().intValue(), 2);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(2).getId().intValue(), 3);

        Long oldCount = relationshipStudentTeacherModel.newQuery().count("*");

        HashMap<String, String> map = new HashMap<>();
        map.put("note", note);
        teacherIntegerRecord.bind("studentsBelongsToMany").attach(Arrays.asList("1", "2", "3", "4", "5"), map);

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

        Record<Teacher, Integer> teacherIntegerRecord = teacherModel.findOrFail(1);

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

        RecordList<Teacher, Integer> all = teacherModel.findAll();

        Student student = studentLongRecord.with("teacher").toObject();


        Assert.assertThrows(RelationAttachException.class, () -> {
            studentLongRecord.bind("teacher").attach(all);
        });

    }


    @Test
    public void attach_HasOneOrMany_单体_单个() {
        Record<Teacher, Integer> teacherRecord = teacherModel.findOrFail(1);

        // 先清除student
        int update = studentModel.newQuery().where("teacher_id", "1").data("teacher_id", "9").update();
        Assert.assertTrue(update > 0);

        Teacher teacher1 = teacherRecord.with("student").toObject();
        System.out.println(teacher1);
        // 老师(id=1)已经有没有学生
        Assert.assertNull(teacher1.getStudent());

        // id=5的学生已经存在,所以应该Update执行没有影响
        Record<StudentModel.Entity, Integer> student = studentModel.findOrFail(5);
        teacherRecord.bind("student").attach(student);

        Teacher teacher2 = teacherRecord.toObject();
        Assert.assertEquals(teacher2.getStudent().getId().intValue(), 5);
    }

    @Test
    public void attach_HasOneOrMany_单体_多个() {
        Record<Teacher, Integer> teacherRecord = teacherModel.findOrFail(1);

        // 先清除student
        int update = studentModel.newQuery().where("teacher_id", "1").data("teacher_id", "9").update();
        Assert.assertTrue(update > 0);

        Teacher teacher1 = teacherRecord.with("student").toObject();
        System.out.println(teacher1);
        // 老师(id=1)已经有没有学生
        Assert.assertNull(teacher1.getStudent());

        RecordList<StudentModel.Entity, Integer> students = studentModel.newQuery()
                .whereIn("id", "1", "2", "3", "4", "5")
                .get();
        teacherRecord.bind("students").attach(students);
        Teacher teacher2 = teacherRecord.toObject();
        Assert.assertNotNull(teacher2.getStudent());
    }

    @Test
    public void attach_HasOneOrMany_集合_单个() {
        Record<Teacher, Integer> teacherRecord = teacherModel.findOrFail(1);

        Teacher teacher1 = teacherRecord.with("students").toObject();
        System.out.println(teacher1);
        // 老师(id=1)已经有2个学生(id=5, id=6)
        Assert.assertEquals(teacher1.getStudents().size(), 2);

        // id=5的学生已经存在,所以应该Update执行没有影响
        Record<StudentModel.Entity, Integer> student = studentModel.findOrFail(5);
        teacherRecord.bind("students").attach(student);

        Teacher teacher2 = teacherRecord.toObject();
        Assert.assertEquals(teacher2.getStudents().size(), 2);


        // id=1的学生不存在,所以应该Update执行
        Record<StudentModel.Entity, Integer> student1 = studentModel.findOrFail(1);
        teacherRecord.bind("students").attach(student1);

        Teacher teacher3 = teacherRecord.toObject();
        Assert.assertEquals(teacher3.getStudents().size(), 3);
        System.out.println(teacher3);
    }

    @Test
    public void attach_HasOneOrMany_集合_多个() {
        Record<Teacher, Integer> teacherRecord = teacherModel.findOrFail(1);

        Teacher teacher1 = teacherRecord.with("students").toObject();
        System.out.println(teacher1);
        // 老师(id=1)已经有2个学生(id=5, id=6)
        Assert.assertEquals(teacher1.getStudents().size(), 2);

        RecordList<StudentModel.Entity, Integer> students = studentModel.newQuery()
                .whereIn("id", "1", "2", "3", "4", "5")
                .get();
        teacherRecord.bind("students").attach(students);

        Teacher teacher2 = teacherRecord.toObject();
        Assert.assertEquals(teacher2.getStudents().size(), 6);
        System.out.println(teacher2);
    }

    @Test
    public void attach_HasOneOrMany_集合_多个id() {
        Record<Teacher, Integer> teacherRecord = teacherModel.findOrFail(1);

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
        Record<Teacher, Integer> teacherIntegerRecord = teacherModel.findOrFail(1);

        Teacher studentsBelongsToMany = teacherIntegerRecord.with("studentsBelongsToMany").toObject();
        // 老师(id=1)已经有3个学生(id=1, id=2, id=3)
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().size(), 3);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(0).getId().intValue(), 1);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(1).getId().intValue(), 2);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(2).getId().intValue(), 3);

        Long oldCount = relationshipStudentTeacherModel.newQuery().count("*");

        // 解除与学生2的关系
        Record<StudentModel.Entity, Integer> student = studentModel.findOrFail(2);
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
        Record<Teacher, Integer> teacherIntegerRecord = teacherModel.findOrFail(1);

        Teacher studentsBelongsToMany = teacherIntegerRecord.with("studentsBelongsToMany").toObject();
        // 老师(id=1)已经有3个学生(id=1, id=2, id=3)
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().size(), 3);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(0).getId().intValue(), 1);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(1).getId().intValue(), 2);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(2).getId().intValue(), 3);

        Long oldCount = relationshipStudentTeacherModel.newQuery().count("*");

        // 解除与学生1,2,4,5的关系, 1,2应该解除, 4,5 无变化
        RecordList<StudentModel.Entity, Integer> records = studentModel.newQuery().whereIn("id", "1", "2", "4", "5").get();
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
        Record<Teacher, Integer> teacherIntegerRecord = teacherModel.findOrFail(1);

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
        Record<Teacher, Integer> teacherIntegerRecord = teacherModel.findOrFail(1);

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
        Record<Teacher, Integer> teacherIntegerRecord = teacherModel.findOrFail(1);

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
        Record<Teacher, Integer> teacherRecord = teacherModel.findOrFail(1);

        Teacher teacher1 = teacherRecord.with("student").toObject();
        System.out.println(teacher1);
        Assert.assertEquals(teacher1.getStudent().getId().intValue(), 5);

        Record<StudentModel.Entity, Integer> studentModelOrFail1 = studentModel.findOrFail(2);
        teacherRecord.bind("student").detach(studentModelOrFail1);

        Teacher teacher11 = teacherRecord.toObject();
        Assert.assertNotNull(teacher11.getStudent());

        Record<StudentModel.Entity, Integer> studentModelOrFail = studentModel.findOrFail(5);
        teacherRecord.bind("student").detach(studentModelOrFail);

        Teacher teacher = teacherRecord.toObject();
        Assert.assertNotEquals(teacher.getStudent().getId().intValue(), 5);
    }

    @Test
    public void detach_HasOneOrMany_单体_单个id() {
        Record<Teacher, Integer> teacherRecord = teacherModel.findOrFail(1);

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
        Record<Teacher, Integer> teacherRecord = teacherModel.findOrFail(1);

        Teacher teacher1 = teacherRecord.with("students").toObject();
        System.out.println(teacher1);
        // 老师(id=1)已经有2个学生(id=5, id=6)
        Assert.assertEquals(teacher1.getStudents().size(), 2);

        Record<StudentModel.Entity, Integer> student = studentModel.findOrFail(2);
        teacherRecord.bind("students").detach(student);

        Teacher teacher2 = teacherRecord.toObject();
        Assert.assertEquals(teacher2.getStudents().size(), 2);

        RecordList<StudentModel.Entity, Integer> records = studentModel.newQuery().whereIn("id", "5").get();
        teacherRecord.bind("students").detach(records);

        Teacher teacher3 = teacherRecord.toObject();
        Assert.assertEquals(teacher3.getStudents().size(), 1);
        System.out.println(teacher3);
    }

    @Test
    public void detach_HasOneOrMany_集合_多个() {
        Record<Teacher, Integer> teacherRecord = teacherModel.findOrFail(1);

        Teacher teacher1 = teacherRecord.with("students").toObject();
        System.out.println(teacher1);
        // 老师(id=1)已经有2个学生(id=5, id=6)
        Assert.assertEquals(teacher1.getStudents().size(), 2);

        Record<StudentModel.Entity, Integer> student = studentModel.findOrFail(2);
        teacherRecord.bind("students").detach(student);

        Teacher teacher2 = teacherRecord.toObject();
        Assert.assertEquals(teacher2.getStudents().size(), 2);

        RecordList<StudentModel.Entity, Integer> records = studentModel.newQuery().whereIn("id", "5", "1", "3").get();
        teacherRecord.bind("students").detach(records);

        Teacher teacher3 = teacherRecord.toObject();
        Assert.assertEquals(teacher3.getStudents().size(), 1);
        System.out.println(teacher3);
    }

    @Test
    public void detach_HasOneOrMany_ALL() {
        Record<Teacher, Integer> teacherRecord = teacherModel.findOrFail(1);

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
        Record<Student, Long>    studentLongRecord    = studentRelationModel.findOrFail(1L);
        Record<Teacher, Integer> teacherIntegerRecord = teacherModel.findOrFail(1);

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
        Record<Student, Long>    studentLongRecord    = studentRelationModel.findOrFail(1L);
        Record<Teacher, Integer> teacherIntegerRecord = teacherModel.findOrFail(1);

        Student student = studentLongRecord.with("teacher").toObject();
        Assert.assertEquals(student.getTeacher().getId().intValue(), 6);
        studentLongRecord.bind("teacher").attach(teacherIntegerRecord);

        Student student1 = studentLongRecord.toObject();
        Assert.assertEquals(student1.getTeacher().getId().intValue(), 1);


        RecordList<Teacher, Integer> all = teacherModel.findAll();
        studentLongRecord.bind("teacher").detach(all);

        Student student2 = studentLongRecord.toObject();
        Assert.assertNull(student2.getTeacher());
    }


    @Test
    public void detach_BelongsTo_单个id() {
        Record<Student, Long>    studentLongRecord    = studentRelationModel.findOrFail(1L);
        Record<Teacher, Integer> teacherIntegerRecord = teacherModel.findOrFail(1);

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
        Record<Student, Long>    studentLongRecord    = studentRelationModel.findOrFail(1L);
        Record<Teacher, Integer> teacherIntegerRecord = teacherModel.findOrFail(1);

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
        Record<Student, Long>    studentLongRecord    = studentRelationModel.findOrFail(1L);
        Record<Teacher, Integer> teacherIntegerRecord = teacherModel.findOrFail(1);

        Student student = studentLongRecord.with("teacher").toObject();
        Assert.assertEquals(student.getTeacher().getId().intValue(), 6);

        studentLongRecord.bind("teacher").detach();

        Student student2 = studentLongRecord.toObject();
        Assert.assertNull(student2.getTeacher());
    }

    @Test
    public void sync_BelongsToMany_单个_and_多个() {
        Record<Teacher, Integer> teacherIntegerRecord = teacherModel.findOrFail(1);

        Teacher studentsBelongsToMany = teacherIntegerRecord.with("studentsBelongsToMany").toObject();
        // 老师(id=1)已经有3个学生(id=1, id=2, id=3)
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().size(), 3);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(0).getId().intValue(), 1);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(1).getId().intValue(), 2);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(2).getId().intValue(), 3);

        Long oldCount = relationshipStudentTeacherModel.newQuery().count("*");

        // 学生2的关系
        Record<StudentModel.Entity, Integer> student = studentModel.findOrFail(2);
        teacherIntegerRecord.bind("studentsBelongsToMany").sync(student);

        Long new1Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(new1Count - oldCount, -2);

        // 学生1,2,3,4的关系
        RecordList<StudentModel.Entity, Integer> records = studentModel.newQuery().whereIn("id", "1", "2", "3", "4").get();
        teacherIntegerRecord.bind("studentsBelongsToMany").sync(records);

        Long new2Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(new2Count - new1Count, 3);

        Teacher teacher = teacherIntegerRecord.toObject();
        Assert.assertEquals(teacher.getStudentsBelongsToMany().size(), 4);
        System.out.println(teacher);
    }

    @Test
    public void sync_BelongsToMany_单个_and_多个_附带信息到中间表() {
        String                   note                 = "ssssss";
        Record<Teacher, Integer> teacherIntegerRecord = teacherModel.findOrFail(1);

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
        Record<StudentModel.Entity, Integer> student = studentModel.findOrFail(2);
        Map<String, String>                  map     = new HashMap<>();
        map.put("note", note);
        int studentsBelongsToMany2 = teacherIntegerRecord.bind("studentsBelongsToMany").sync(student, map);

        Long new1Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(studentsBelongsToMany2, 2);
        Assert.assertEquals(new1Count - oldCount, -2);

        // 学生1,2,3,4的关系
        RecordList<StudentModel.Entity, Integer> records =
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
        Record<Teacher, Integer> teacherIntegerRecord = teacherModel.findOrFail(1);

        Teacher studentsBelongsToMany = teacherIntegerRecord.with("studentsBelongsToMany").toObject();
        // 老师(id=1)已经有3个学生(id=1, id=2, id=3)
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().size(), 3);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(0).getId().intValue(), 1);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(1).getId().intValue(), 2);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(2).getId().intValue(), 3);

        Long oldCount = relationshipStudentTeacherModel.newQuery().count("*");

        // 学生2的关系
        teacherIntegerRecord.bind("studentsBelongsToMany").sync("2");

        Long new1Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(new1Count - oldCount, -2);

        // 学生1,2,3,4的关系
        teacherIntegerRecord.bind("studentsBelongsToMany").sync(Arrays.asList("1", "2", "3", "4"));

        Long new2Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(new2Count - new1Count, 3);

        Teacher teacher = teacherIntegerRecord.toObject();
        Assert.assertEquals(teacher.getStudentsBelongsToMany().size(), 4);
        System.out.println(teacher);
    }

    @Test
    public void sync_BelongsToMany_单个_and_多个ID_附带信息到中间表() {
        String                   note                 = "ssssss";
        Record<Teacher, Integer> teacherIntegerRecord = teacherModel.findOrFail(1);

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
        Map<String, String> map = new HashMap<>();
        map.put("note", note);
        int studentsBelongsToMany2 = teacherIntegerRecord.bind("studentsBelongsToMany").sync("2", map);

        Long new1Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(studentsBelongsToMany2, 2);
        Assert.assertEquals(new1Count - oldCount, -2);

        // 学生1,2,3,4的关系
        int studentsBelongsToMany1 =
                teacherIntegerRecord.bind("studentsBelongsToMany").sync(Arrays.asList("1", "2", "3", "4"), map);
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
        Record<Teacher, Integer> teacherRecord = teacherModel.findOrFail(1);

        teacherRecord.with("student").toObject();

        Record<StudentModel.Entity, Integer> student = studentModel.findOrFail(5);
        teacherRecord.bind("student").sync(student);

        Teacher teacher2 = teacherRecord.toObject();
        Assert.assertEquals(teacher2.getStudent().getId().intValue(), 5);

        Long teacher_id = studentModel.newQuery().where("teacher_id", "1").count("*");
        Assert.assertEquals(teacher_id.intValue(), 1);
    }

    @Test
    public void sync_HasOneOrMany_单体_多个() {
        Record<Teacher, Integer> teacherRecord = teacherModel.findOrFail(1);

        Teacher teacher1 = teacherRecord.with("student").toObject();

        RecordList<StudentModel.Entity, Integer> students = studentModel.newQuery()
                .whereIn("id", "1", "2", "3", "4", "5")
                .get();
        teacherRecord.bind("students").sync(students);
        Teacher teacher2 = teacherRecord.toObject();
        Assert.assertNotNull(teacher2.getStudent());
    }

    @Test
    public void sync_HasOneOrMany_集合_单个() {
        Record<Teacher, Integer> teacherRecord = teacherModel.findOrFail(1);

        Teacher teacher1 = teacherRecord.with("students").toObject();
        System.out.println(teacher1);
        // 老师(id=1)已经有2个学生(id=5, id=6)
        Assert.assertEquals(teacher1.getStudents().size(), 2);

        Record<StudentModel.Entity, Integer> student  = studentModel.findOrFail(5);
        int                                  students = teacherRecord.bind("students").sync(student);
        Assert.assertEquals(students, 1);

        Teacher teacher2 = teacherRecord.toObject();
        Assert.assertEquals(teacher2.getStudents().size(), 1);

        Record<StudentModel.Entity, Integer> student1  = studentModel.findOrFail(1);
        int                                  students1 = teacherRecord.bind("students").sync(student1);
        Assert.assertEquals(students1, 2);

        Teacher teacher3 = teacherRecord.toObject();
        Assert.assertEquals(teacher3.getStudents().size(), 1);
        System.out.println(teacher3);
    }

    @Test
    public void sync_HasOneOrMany_集合_多个() {
        Record<Teacher, Integer> teacherRecord = teacherModel.findOrFail(1);

        Teacher teacher1 = teacherRecord.with("students").toObject();
        System.out.println(teacher1);
        // 老师(id=1)已经有2个学生(id=5, id=6)
        Assert.assertEquals(teacher1.getStudents().size(), 2);

        RecordList<StudentModel.Entity, Integer> students = studentModel.newQuery()
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
        Record<Teacher, Integer> teacherRecord = teacherModel.findOrFail(1);

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
        Record<Student, Long>    studentLongRecord    = studentRelationModel.findOrFail(1L);
        Record<Teacher, Integer> teacherIntegerRecord = teacherModel.findOrFail(1);
        Student                  student              = studentLongRecord.with("teacher").toObject();
        Assert.assertEquals(student.getTeacher().getId().intValue(), 6);
        studentLongRecord.bind("teacher").sync(teacherIntegerRecord);
        Student student1 = studentLongRecord.toObject();
        Assert.assertEquals(student1.getTeacher().getId().intValue(), 1);
    }

    @Test
    public void sync_BelongsTo_单个id() {
        Record<Student, Long> studentLongRecord = studentRelationModel.findOrFail(1L);
        Student               student           = studentLongRecord.with("teacher").toObject();
        Assert.assertEquals(student.getTeacher().getId().intValue(), 6);
        studentLongRecord.bind("teacher").sync("1");
        Student student1 = studentLongRecord.toObject();
        Assert.assertEquals(student1.getTeacher().getId().intValue(), 1);
    }

    @Test
    public void sync_BelongsTo_异常() {
        Record<Student, Long>        studentLongRecord = studentRelationModel.findOrFail(1L);
        RecordList<Teacher, Integer> all               = teacherModel.findAll();
        Student                      student           = studentLongRecord.with("teacher").toObject();
        Assert.assertThrows(RelationAttachException.class, () -> {
            studentLongRecord.bind("teacher").sync(all);
        });
    }


    @Test
    public void toggle_BelongsToMany_单个_and_多个() {
        Record<Teacher, Integer> teacherIntegerRecord = teacherModel.findOrFail(1);

        Teacher studentsBelongsToMany = teacherIntegerRecord.with("studentsBelongsToMany").toObject();
        // 老师(id=1)已经有3个学生(id=1, id=2, id=3)
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().size(), 3);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(0).getId().intValue(), 1);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(1).getId().intValue(), 2);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(2).getId().intValue(), 3);

        Long oldCount = relationshipStudentTeacherModel.newQuery().count("*");

        // 学生2的关系
        Record<StudentModel.Entity, Integer> student = studentModel.findOrFail(2);
        // 反转2的关系, 即解除2的关系
        // 剩余 1,3
        teacherIntegerRecord.bind("studentsBelongsToMany").toggle(student);

        Long new1Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(new1Count - oldCount, -1);

        RecordList<StudentModel.Entity, Integer> records = studentModel.newQuery().whereIn("id", "1", "2", "3", "4").get();
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
        String                   note                 = "ssssss";
        Record<Teacher, Integer> teacherIntegerRecord = teacherModel.findOrFail(1);

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
        Record<StudentModel.Entity, Integer> student = studentModel.findOrFail(2);
        Map<String, String>                  map     = new HashMap<>();
        map.put("note", note);
        int studentsBelongsToMany2 = teacherIntegerRecord.bind("studentsBelongsToMany").toggle(student, map);

        Long new1Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(studentsBelongsToMany2, 1);
        Assert.assertEquals(new1Count - oldCount, -1);

        // 学生1,2,3,4的关系
        RecordList<StudentModel.Entity, Integer> records =
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
        Record<Teacher, Integer> teacherIntegerRecord = teacherModel.findOrFail(1);

        Teacher studentsBelongsToMany = teacherIntegerRecord.with("studentsBelongsToMany").toObject();
        // 老师(id=1)已经有3个学生(id=1, id=2, id=3)
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().size(), 3);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(0).getId().intValue(), 1);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(1).getId().intValue(), 2);
        Assert.assertEquals(studentsBelongsToMany.getStudentsBelongsToMany().get(2).getId().intValue(), 3);

        Long oldCount = relationshipStudentTeacherModel.newQuery().count("*");

        // 学生2的关系
        teacherIntegerRecord.bind("studentsBelongsToMany").toggle("2");

        Long new1Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(new1Count - oldCount, -1);

        // 学生1,2,3,4的关系
        teacherIntegerRecord.bind("studentsBelongsToMany").toggle(Arrays.asList("1", "2", "3", "4"));

        Long new2Count = relationshipStudentTeacherModel.newQuery().count("*");
        Assert.assertEquals(new2Count - new1Count, 0);

        Teacher teacher = teacherIntegerRecord.toObject();
        Assert.assertEquals(teacher.getStudentsBelongsToMany().size(), 2);
        System.out.println(teacher);
    }

    @Test
    public void toggle_BelongsToMany_单个_and_多个ID_附带信息到中间表() {
        String                   note                 = "ssssss";
        Record<Teacher, Integer> teacherIntegerRecord = teacherModel.findOrFail(1);

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
        Map<String, String> map = new HashMap<>();
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
        Record<Teacher, Integer> teacherRecord = teacherModel.findOrFail(1);
        // 老师(id=1)已经有2个学生(id=5, id=6)

        teacherRecord.with("student").toObject();

        Record<StudentModel.Entity, Integer> student = studentModel.findOrFail(5);
        teacherRecord.bind("student").toggle(student);

        Teacher teacher2 = teacherRecord.toObject();
        Assert.assertEquals(teacher2.getStudent().getId().intValue(), 6);

        Long teacher_id = studentModel.newQuery().where("teacher_id", "1").count("*");
        Assert.assertEquals(teacher_id.intValue(), 1);
    }

    @Test
    public void toggle_HasOneOrMany_单体_多个() {
        Record<Teacher, Integer> teacherRecord = teacherModel.findOrFail(1);
        // 老师(id=1)已经有2个学生(id=5, id=6)

        Teacher teacher1 = teacherRecord.with("student").toObject();

        RecordList<StudentModel.Entity, Integer> students = studentModel.newQuery()
                .whereIn("id", "1", "2", "3", "4", "5")
                .get();
        // 切换
        teacherRecord.bind("students").toggle(students);
        Teacher teacher2 = teacherRecord.toObject();
        Assert.assertNotNull(teacher2.getStudent());
    }

    @Test
    public void toggle_HasOneOrMany_集合_单个() {
        Record<Teacher, Integer> teacherRecord = teacherModel.findOrFail(1);

        Teacher teacher1 = teacherRecord.with("students").toObject();
        System.out.println(teacher1);
        // 老师(id=1)已经有2个学生(id=5, id=6)
        Assert.assertEquals(teacher1.getStudents().size(), 2);

        Record<StudentModel.Entity, Integer> student  = studentModel.findOrFail(5);
        int                                  students = teacherRecord.bind("students").toggle(student);
        Assert.assertEquals(students, 1);

        Teacher teacher2 = teacherRecord.toObject();
        Assert.assertEquals(teacher2.getStudents().size(), 1);

        Record<StudentModel.Entity, Integer> student1  = studentModel.findOrFail(1);
        int                                  students1 = teacherRecord.bind("students").toggle(student1);
        Assert.assertEquals(students1, 1);

        Teacher teacher3 = teacherRecord.toObject();
        Assert.assertEquals(teacher3.getStudents().size(), 2);
        System.out.println(teacher3);
    }

    @Test
    public void toggle_HasOneOrMany_集合_多个() {
        Record<Teacher, Integer> teacherRecord = teacherModel.findOrFail(1);

        Teacher teacher1 = teacherRecord.with("students", builder -> builder.orderBy("id", OrderBy.ASC)).toObject();
        System.out.println(teacher1);
        // 老师(id=1)已经有2个学生(id=5, id=6)
        Assert.assertEquals(teacher1.getStudents().size(), 2);

        RecordList<StudentModel.Entity, Integer> students = studentModel.newQuery()
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
        Record<Teacher, Integer> teacherRecord = teacherModel.findOrFail(1);

        Teacher teacher1 = teacherRecord.with("students", builder -> builder.orderBy("id", OrderBy.ASC)).toObject();
        System.out.println(teacher1);
        // 老师(id=1)已经有2个学生(id=5, id=6)
        Assert.assertEquals(teacher1.getStudents().size(), 2);

        int students = teacherRecord.bind("students").toggle(Arrays.asList("1", "2", "3", "4", "5"));
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
        Record<Student, Long>    studentLongRecord    = studentRelationModel.findOrFail(1L);
        Record<Teacher, Integer> teacherIntegerRecord = teacherModel.findOrFail(1);
        Student                  student              = studentLongRecord.with("teacher").toObject();
        Assert.assertEquals(student.getTeacher().getId().intValue(), 6);
        studentLongRecord.bind("teacher").toggle(teacherIntegerRecord);
        Student student1 = studentLongRecord.toObject();
        Assert.assertEquals(student1.getTeacher().getId().intValue(), 1);
    }

    @Test
    public void toggle_BelongsTo_单个id() {
        Record<Student, Long> studentLongRecord = studentRelationModel.findOrFail(1L);
        Student               student           = studentLongRecord.with("teacher").toObject();
        Assert.assertEquals(student.getTeacher().getId().intValue(), 6);
        studentLongRecord.bind("teacher").toggle("1");
        Student student1 = studentLongRecord.toObject();
        Assert.assertEquals(student1.getTeacher().getId().intValue(), 1);
    }

    @Test
    public void toggle_BelongsTo_异常() {
        Record<Student, Long>        studentLongRecord = studentRelationModel.findOrFail(1L);
        RecordList<Teacher, Integer> all               = teacherModel.findAll();
        Student                      student           = studentLongRecord.with("teacher").toObject();
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

}
