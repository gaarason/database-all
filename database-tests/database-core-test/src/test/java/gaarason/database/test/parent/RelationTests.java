package gaarason.database.test.parent;

import gaarason.database.appointment.OrderBy;
import gaarason.database.appointment.Paginate;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
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

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
abstract public class RelationTests extends BaseTests {

    protected static final StudentModel studentModel = new StudentModel();

    protected static final TeacherModel teacherModel = new TeacherModel();

    protected static RelationshipStudentTeacherModel relationshipStudentTeacherModel =
        new RelationshipStudentTeacherModel();

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    protected GaarasonDataSource getGaarasonDataSource() {
        return studentModel.getGaarasonDataSource();
    }
    @Override
    protected List<TABLE> getInitTables() {
        return Arrays.asList(TABLE.student, TABLE.teacher, TABLE.relationship_student_teacher);
    }
    @Test
    public void 一对一关系() {
        // 声明但不使用
        Student student = studentModel.newQuery().firstOrFail().toObject();
        System.out.println(student);
        Assert.assertNull(student.getTeacher());
        Assert.assertNull(student.getRelationshipStudentTeachers());

        // 声明且使用
        Student student2 = studentModel.newQuery().firstOrFail().with("teacher").toObject();
        System.out.println(student2);
        Assert.assertNotNull(student2.getTeacher());
        Assert.assertNull(student2.getRelationshipStudentTeachers());
        Assert.assertEquals((long) student2.getTeacher().getId(), 6);
        Assert.assertNull(student2.getTeacher().getStudents());
        Assert.assertNull(student2.getTeacher().getStudent());

        // 声明且使用
        Student student3 = studentModel.newQuery().with(Student::getTeacher).firstOrFail().toObject();
        System.out.println(student3);
        Assert.assertNotNull(student3.getTeacher());
        Assert.assertNull(student3.getRelationshipStudentTeachers());
        Assert.assertEquals((long) student3.getTeacher().getId(), 6);
        Assert.assertNull(student3.getTeacher().getStudents());
        Assert.assertNull(student3.getTeacher().getStudent());

        // 声明且使用
        Student student4 = studentModel.newQuery().with(Student::getTeacher).queryOrFail("select * from student limit 1").toObject();
        System.out.println(student4);
        Assert.assertNotNull(student4.getTeacher());
        Assert.assertNull(student4.getRelationshipStudentTeachers());
        Assert.assertEquals((long) student4.getTeacher().getId(), 6);
        Assert.assertNull(student4.getTeacher().getStudents());
        Assert.assertNull(student4.getTeacher().getStudent());
    }

    @Test
    public void 一对一关系_子关系不存在() {
        // 声明且使用
        Student student1 =
            studentModel.newQuery().firstOrFail().with(Student::getTeacher, builder -> builder.where("id", "99"),
                record -> record.with("students", builder -> builder, record1 -> record1.with("teacher"))).toObject();
        System.out.println(student1);
        Assert.assertNull(student1.getTeacher());

        // 声明且使用
        Student student2 =
            studentModel.newQuery().firstOrFail().with("teacher", builder -> builder.where("id", "99"),
                record -> record.with("students", builder -> builder, record1 -> record1.with("teacher"))).toObject();
        System.out.println(student2);
        Assert.assertNull(student2.getTeacher());
    }


    @Test
    public void 自定义关系() {
        // 声明且使用
        Student student = studentModel.findOrFail(2)
            .with(Student::getTeachersBelongsToMany)
            .with(Student::getRelationshipStudentTeachers)
            .with(Student::getRelationshipStudentTeacher)
            .toObject();

        Assert.assertEquals(2, student.getTeachersBelongsToMany().size());
        Assert.assertEquals(2, student.getRelationshipStudentTeachers().size());
        Assert.assertNotNull( student.getRelationshipStudentTeacher());
        Assert.assertEquals(2, student.getRelationshipStudentTeacher().getStudentId().intValue());
    }

    @Test
    public void 一对一关系_builder筛选() {
        Student student2 =
            studentModel.newQuery()
                .firstOrFail()
                .with("teacher", (builder -> builder.where("id", "!=", "6")))
                .toObject();
        System.out.println(student2);
        Assert.assertNull(student2.getTeacher());

        List<Teacher> teachers = teacherModel.newQuery()
            .where("id", "2")
            .get()
            .with("students", builder -> builder.where("id", "!=", "4"))
            .toObjectList();

        System.out.println(teachers);

    }

    @Test
    public void 一对一关系_无线级关系() {
        // select * from student limit 1
        // select * from teacher where id in (?)
        // select * from student where id in (? ?)
        Student student2 =
            studentModel.newQuery().where("id", "1").where("teacher_id", "6")
                .firstOrFail()
                .with("teacher", builder -> builder, record -> record.with("student", builder -> builder,
                    record1 -> record1.with("teacher", builder -> builder, record2 -> record2.with("student",
                        builder -> builder, record3 -> record3.with("teacher")))))
                .toObject();
        System.out.println(student2);
        Assert.assertEquals(student2.getId().intValue(), 1);
        Assert.assertEquals(student2.getTeacherId().intValue(), 6);
        Assert.assertNotNull(student2.getTeacher());
        Assert.assertNull(student2.getRelationshipStudentTeachers());
        Assert.assertNotNull(student2.getTeacher().getStudent());
        Assert.assertEquals(student2.getTeacher().getStudent().getTeacherId().intValue(), 6);
        Assert.assertNotNull(student2.getTeacher().getStudent().getTeacher());
        Assert.assertEquals(student2.getTeacher().getStudent().getTeacher().getId().intValue(), 6);
        Assert.assertNotNull(student2.getTeacher().getStudent().getTeacher().getStudent());
        Assert.assertNull(student2.getTeacher().getStudent().getTeacher().getStudents());
        Assert.assertEquals(student2.getTeacher().getStudent().getTeacher().getStudent().getTeacherId().intValue(), 6);
        Assert.assertNotNull(student2.getTeacher().getStudent().getTeacher().getStudent().getTeacher());
        Assert.assertEquals(
            student2.getTeacher().getStudent().getTeacher().getStudent().getTeacher().getId().intValue(),
            6);
        Assert.assertNull(student2.getTeacher().getStudent().getTeacher().getStudent().getTeacher().getStudent());
        Assert.assertNull(student2.getTeacher().getStudent().getTeacher().getStudent().getTeacher().getStudents());

    }


    @Test
    public void 一对一关系_一对多关系_同级关系() {
        Student student =
            studentModel.newQuery().where("id", "1")
                .firstOrFail()
                .with("teacher")
                .with("relationshipStudentTeachers")
                .toObject();
        System.out.println(student);
        Assert.assertNotNull(student.getTeacher());
        Assert.assertEquals(student.getTeacher().getId().intValue(), 6);
        Assert.assertNotNull(student.getRelationshipStudentTeachers());
        Assert.assertEquals(student.getRelationshipStudentTeachers().get(0).getStudentId().intValue(), 1);
    }


    @Test
    public void 一对一关系_一对多关系_同级关系2() {
        Student student =
            studentModel.newQuery().where("id", "1")
                .with("teacher")
                .with("relationshipStudentTeachers")
                .firstOrFail()
                .toObject();
        System.out.println(student);
        Assert.assertNotNull(student.getTeacher());
        Assert.assertEquals(student.getTeacher().getId().intValue(), 6);
        Assert.assertNotNull(student.getRelationshipStudentTeachers());
        Assert.assertEquals(student.getRelationshipStudentTeachers().get(0).getStudentId().intValue(), 1);
    }


    @Test
    public void 一对一关系_一对多关系_快捷用法() {
        Student student =
            studentModel.newQuery().where("id", "1")
                .firstOrFail()
                .with("teacher.student", builder -> builder.whereIn("id", "1", "2", "3"))
                .with("relationshipStudentTeachers", builder -> builder.whereNotIn("id", "13"))
                .toObject();
        System.out.println(student);
    }


    @Test
    public void 一对一关系_一对多关系_快捷用法2() {
        Student student =
            studentModel.newQuery().where("id", "1")
                .with("teacher.student", builder -> builder.whereIn("id", "1", "2", "3"))
                .with("relationshipStudentTeachers", builder -> builder.whereNotIn("id", "13"))
                .firstOrFail()
                .toObject();
        System.out.println(student);
    }

    @Test
    public void 一对一关系_一对多关系_无线级关系() {

        Student student3 =
            studentModel.newQuery()
                .firstOrFail()
                .with("teacher", builder -> builder, record -> record.with("students", builder -> builder,
                    record1 -> record1.with("teacher", builder -> builder, record2 -> record2.with("students",
                        builder -> builder, record3 -> record3.with("teacher")))))
                .toObject();
        System.out.println(student3);

        Assert.assertEquals(student3.getId().intValue(), 1);
        Assert.assertEquals(student3.getTeacherId().intValue(), 6);
        Assert.assertNotNull(student3.getTeacher());
        Assert.assertNull(student3.getRelationshipStudentTeachers());
        Assert.assertNull(student3.getTeacher().getStudent());
        Assert.assertNotNull(student3.getTeacher().getStudents());
        Assert.assertEquals(student3.getTeacher().getStudents().size(), 4);

        for (int i = 0; i < 4; i++) {


            System.out.println("=========== i = " + i + " start =============");

            Assert.assertEquals(student3.getTeacher().getStudents().get(i).getTeacherId().intValue(), 6);
            Assert.assertNotNull(student3.getTeacher().getStudents().get(i).getTeacher());
            Assert.assertEquals(student3.getTeacher().getStudents().get(i).getTeacher().getId().intValue(), 6);

            System.out.println("========= a =========");
            System.out.println(student3.getTeacher());
            System.out.println(student3.getTeacher().getStudents());
            System.out.println(student3.getTeacher().getStudents().get(i));
            System.out.println(student3.getTeacher().getStudents().get(i).getTeacher());
            System.out.println("========= b =========");

            Assert.assertNotNull(student3.getTeacher().getStudents().get(i).getTeacher().getStudents());

            Assert.assertNull(student3.getTeacher().getStudents().get(i).getTeacher().getStudent());
            Assert.assertEquals(
                student3.getTeacher().getStudents().get(i).getTeacher().getStudents().get(i).getTeacherId().intValue(),
                6);
            Assert.assertNotNull(
                student3.getTeacher().getStudents().get(i).getTeacher().getStudents().get(i).getTeacher());
            Assert.assertEquals(
                student3.getTeacher()
                    .getStudents()
                    .get(i)
                    .getTeacher()
                    .getStudents()
                    .get(i)
                    .getTeacher()
                    .getId()
                    .intValue(),
                6);
            Assert.assertNull(
                student3.getTeacher().getStudents().get(i).getTeacher().getStudents().get(i).getTeacher().getStudent());
            Assert.assertNull(student3.getTeacher()
                .getStudents()
                .get(i)
                .getTeacher()
                .getStudents()
                .get(i)
                .getTeacher()
                .getStudents());

            System.out.println("=========== i = " + i + " end =============\n");
        }

    }

    @Test
    public void 一对一关系_一对多关系_无线级关系2() {

        Student student3 =
            studentModel.newQuery()
                .with("teacher", builder -> builder, record -> record.with("students", builder -> builder,
                    record1 -> record1.with("teacher", builder -> builder, record2 -> record2.with("students",
                        builder -> builder, record3 -> record3.with("teacher")))))
                .firstOrFail()
                .toObject();
        System.out.println(student3);

        Assert.assertEquals(student3.getId().intValue(), 1);
        Assert.assertEquals(student3.getTeacherId().intValue(), 6);
        Assert.assertNotNull(student3.getTeacher());
        Assert.assertNull(student3.getRelationshipStudentTeachers());
        Assert.assertNull(student3.getTeacher().getStudent());
        Assert.assertNotNull(student3.getTeacher().getStudents());
        Assert.assertEquals(student3.getTeacher().getStudents().size(), 4);

        for (int i = 0; i < 4; i++) {


            System.out.println("=========== i = " + i + " start =============");

            Assert.assertEquals(student3.getTeacher().getStudents().get(i).getTeacherId().intValue(), 6);
            Assert.assertNotNull(student3.getTeacher().getStudents().get(i).getTeacher());
            Assert.assertEquals(student3.getTeacher().getStudents().get(i).getTeacher().getId().intValue(), 6);

            System.out.println("========= a =========");
            System.out.println(student3.getTeacher());
            System.out.println(student3.getTeacher().getStudents());
            System.out.println(student3.getTeacher().getStudents().get(i));
            System.out.println(student3.getTeacher().getStudents().get(i).getTeacher());
            System.out.println("========= b =========");

            Assert.assertNotNull(student3.getTeacher().getStudents().get(i).getTeacher().getStudents());

            Assert.assertNull(student3.getTeacher().getStudents().get(i).getTeacher().getStudent());
            Assert.assertEquals(
                student3.getTeacher().getStudents().get(i).getTeacher().getStudents().get(i).getTeacherId().intValue(),
                6);
            Assert.assertNotNull(
                student3.getTeacher().getStudents().get(i).getTeacher().getStudents().get(i).getTeacher());
            Assert.assertEquals(
                student3.getTeacher()
                    .getStudents()
                    .get(i)
                    .getTeacher()
                    .getStudents()
                    .get(i)
                    .getTeacher()
                    .getId()
                    .intValue(),
                6);
            Assert.assertNull(
                student3.getTeacher().getStudents().get(i).getTeacher().getStudents().get(i).getTeacher().getStudent());
            Assert.assertNull(student3.getTeacher()
                .getStudents()
                .get(i)
                .getTeacher()
                .getStudents()
                .get(i)
                .getTeacher()
                .getStudents());

            System.out.println("=========== i = " + i + " end =============\n");
        }

    }


    @Test
    public void 一对一关系_一对多关系_无线级关系_快捷用法() {

        Student student3 =
            studentModel.newQuery()
                .firstOrFail()
                .with("teacher.students.teacher.students.teacher")
                .toObject();
        System.out.println(student3);

        Assert.assertEquals(student3.getId().intValue(), 1);
        Assert.assertEquals(student3.getTeacherId().intValue(), 6);
        Assert.assertNotNull(student3.getTeacher());
        Assert.assertNull(student3.getRelationshipStudentTeachers());
        Assert.assertNull(student3.getTeacher().getStudent());
        Assert.assertNotNull(student3.getTeacher().getStudents());
        Assert.assertEquals(student3.getTeacher().getStudents().size(), 4);

        for (int i = 0; i < 4; i++) {


            System.out.println("=========== i = " + i + " start =============");

            Assert.assertEquals(student3.getTeacher().getStudents().get(i).getTeacherId().intValue(), 6);
            Assert.assertNotNull(student3.getTeacher().getStudents().get(i).getTeacher());
            Assert.assertEquals(student3.getTeacher().getStudents().get(i).getTeacher().getId().intValue(), 6);

            System.out.println("========= a =========");
            System.out.println(student3.getTeacher());
            System.out.println(student3.getTeacher().getStudents());
            System.out.println(student3.getTeacher().getStudents().get(i));
            System.out.println(student3.getTeacher().getStudents().get(i).getTeacher());
            System.out.println("========= b =========");

            Assert.assertNotNull(student3.getTeacher().getStudents().get(i).getTeacher().getStudents());

            Assert.assertNull(student3.getTeacher().getStudents().get(i).getTeacher().getStudent());
            Assert.assertEquals(
                student3.getTeacher().getStudents().get(i).getTeacher().getStudents().get(i).getTeacherId().intValue(),
                6);
            Assert.assertNotNull(
                student3.getTeacher().getStudents().get(i).getTeacher().getStudents().get(i).getTeacher());
            Assert.assertEquals(
                student3.getTeacher()
                    .getStudents()
                    .get(i)
                    .getTeacher()
                    .getStudents()
                    .get(i)
                    .getTeacher()
                    .getId()
                    .intValue(),
                6);
            Assert.assertNull(
                student3.getTeacher().getStudents().get(i).getTeacher().getStudents().get(i).getTeacher().getStudent());
            Assert.assertNull(student3.getTeacher()
                .getStudents()
                .get(i)
                .getTeacher()
                .getStudents()
                .get(i)
                .getTeacher()
                .getStudents());

            System.out.println("=========== i = " + i + " end =============\n");
        }

    }

    @Test
    public void 一对一关系_一对多关系_无线级关系_builder筛选() {

        Student student3 =
            studentModel.newQuery()
                .firstOrFail()
                .with("teacher", builder -> builder, record -> record.with("students", builder -> builder,
                    record1 -> record1.with("teacher", builder -> builder, record2 -> record2.with("students",
                        builder -> builder.whereIn("id", "3", "2"), record3 -> record3.with("teacher")))))
                .toObject();
        System.out.println(student3);

        Assert.assertEquals(student3.getId().intValue(), 1);
        Assert.assertEquals(student3.getTeacherId().intValue(), 6);
        Assert.assertNotNull(student3.getTeacher());
        Assert.assertNull(student3.getRelationshipStudentTeachers());
        Assert.assertNull(student3.getTeacher().getStudent());
        Assert.assertNotNull(student3.getTeacher().getStudents());
        Assert.assertEquals(student3.getTeacher().getStudents().size(), 4);
        // 主要是这行
        Assert.assertEquals(student3.getTeacher().getStudents().get(0).getTeacher().getStudents().size(), 2);
    }


    @Test
    public void 一对一关系_一对多关系_无线级关系_builder筛选2() {
        // 明显性能问题
        Student student3 =
            studentModel.newQuery()
                .firstOrFail()
                .with("teacher", builder -> builder, record0 -> record0.with("students", builder -> builder,
                    record1 -> record1.with("relationshipStudentTeachers", builder -> builder,
                        record2 -> record2.with("teacher",
                            builder -> builder, record3 -> record3.with("students", builder -> builder,
                                record4 -> record4.with("teacher", builder -> builder,
                                    record5 -> record5.with("students", builder -> builder,
                                        record6 -> record6.with("teacher",
                                            builder -> builder, record7 -> record7.with("students",
                                                builder -> builder.whereIn("id", "3", "2"),
                                                record8 -> record8.with("teacher")))))))))).toObject();
        // 同样的断言
        assert1(student3);

    }

    @Test
    public void 一对一关系_一对多关系_无线级关系_builder筛选2_快捷方式() {
        Student student3 =
            studentModel.newQuery()
                .firstOrFail()
                .with("teacher.students.relationshipStudentTeachers.teacher.students.teacher.students.teacher.students",
                    builder -> builder.whereIn("id", "3", "2"),
                    record8 -> record8.with("teacher")).toObject();
        // 同样的断言
        assert1(student3);
    }

    @Test
    public void 一对一关系_一对多关系_无线级关系_builder筛选2_快捷方式2() {
        Student student3 =
            studentModel.newQuery()
                .firstOrFail()
                .with("teacher.students.relationshipStudentTeachers", builder -> builder, record -> record.with(
                    "teacher.students.teacher.students.teacher.students",
                    builder -> builder.whereIn("id", "3", "2"),
                    record8 -> record8.with("teacher"))).toObject();

        // 同样的断言
        assert1(student3);
    }

    private void assert1(Student student3) {
        System.out.println(student3);
        Assert.assertEquals(student3.getId().intValue(), 1);
        Assert.assertEquals(student3.getTeacherId().intValue(), 6);
        Assert.assertNotNull(student3.getTeacher());
        Assert.assertNull(student3.getRelationshipStudentTeachers());
        Assert.assertNull(student3.getTeacher().getStudent());
        Assert.assertEquals(student3.getTeacher().getStudents().size(), 4);
        Assert.assertEquals(student3.getTeacher().getStudents().get(0).getRelationshipStudentTeachers().size(), 2);
        Assert.assertEquals(student3.getTeacher().getStudents().get(1).getRelationshipStudentTeachers().size(), 2);
        Assert.assertEquals(student3.getTeacher().getStudents().get(2).getRelationshipStudentTeachers().size(), 2);
        Assert.assertEquals(student3.getTeacher().getStudents().get(3).getRelationshipStudentTeachers().size(), 2);
        Assert.assertEquals(
            student3.getTeacher().getStudents().get(0).getRelationshipStudentTeachers().get(0).getId().intValue(), 1);
        Assert.assertEquals(
            student3.getTeacher().getStudents().get(0).getRelationshipStudentTeachers().get(1).getId().intValue(), 2);
        Assert.assertEquals(
            student3.getTeacher().getStudents().get(1).getRelationshipStudentTeachers().get(0).getId().intValue(), 3);
        Assert.assertEquals(
            student3.getTeacher().getStudents().get(1).getRelationshipStudentTeachers().get(1).getId().intValue(), 4);
        Assert.assertEquals(
            student3.getTeacher().getStudents().get(2).getRelationshipStudentTeachers().get(0).getId().intValue(), 5);
        Assert.assertEquals(
            student3.getTeacher().getStudents().get(2).getRelationshipStudentTeachers().get(1).getId().intValue(), 6);
        Assert.assertEquals(
            student3.getTeacher().getStudents().get(3).getRelationshipStudentTeachers().get(0).getId().intValue(), 7);
        Assert.assertEquals(
            student3.getTeacher().getStudents().get(3).getRelationshipStudentTeachers().get(1).getId().intValue(), 8);

        Assert.assertEquals(student3.getTeacher()
            .getStudents()
            .get(0)
            .getRelationshipStudentTeachers()
            .get(0)
            .getTeacher()
            .getId()
            .intValue(), 1);
        Assert.assertEquals(student3.getTeacher()
            .getStudents()
            .get(0)
            .getRelationshipStudentTeachers()
            .get(1)
            .getTeacher()
            .getId()
            .intValue(), 2);
        Assert.assertEquals(student3.getTeacher()
            .getStudents()
            .get(1)
            .getRelationshipStudentTeachers()
            .get(0)
            .getTeacher()
            .getId()
            .intValue(), 1);
        Assert.assertEquals(student3.getTeacher()
            .getStudents()
            .get(1)
            .getRelationshipStudentTeachers()
            .get(1)
            .getTeacher()
            .getId()
            .intValue(), 2);
        Assert.assertEquals(student3.getTeacher()
            .getStudents()
            .get(2)
            .getRelationshipStudentTeachers()
            .get(0)
            .getTeacher()
            .getId()
            .intValue(), 1);
        Assert.assertEquals(student3.getTeacher()
            .getStudents()
            .get(2)
            .getRelationshipStudentTeachers()
            .get(1)
            .getTeacher()
            .getId()
            .intValue(), 2);
        Assert.assertEquals(student3.getTeacher()
            .getStudents()
            .get(3)
            .getRelationshipStudentTeachers()
            .get(0)
            .getTeacher()
            .getId()
            .intValue(), 6);
        Assert.assertEquals(student3.getTeacher()
            .getStudents()
            .get(3)
            .getRelationshipStudentTeachers()
            .get(1)
            .getTeacher()
            .getId()
            .intValue(), 2);

        Assert.assertEquals(student3.getTeacher()
            .getStudents()
            .get(0)
            .getRelationshipStudentTeachers()
            .get(0)
            .getTeacher()
            .getStudents()
            .size(), 2);
        Assert.assertEquals(student3.getTeacher()
            .getStudents()
            .get(0)
            .getRelationshipStudentTeachers()
            .get(1)
            .getTeacher()
            .getStudents()
            .size(), 2);
        Assert.assertEquals(student3.getTeacher()
            .getStudents()
            .get(1)
            .getRelationshipStudentTeachers()
            .get(0)
            .getTeacher()
            .getStudents()
            .size(), 2);
        Assert.assertEquals(student3.getTeacher()
            .getStudents()
            .get(1)
            .getRelationshipStudentTeachers()
            .get(1)
            .getTeacher()
            .getStudents()
            .size(), 2);
        Assert.assertEquals(student3.getTeacher()
            .getStudents()
            .get(2)
            .getRelationshipStudentTeachers()
            .get(0)
            .getTeacher()
            .getStudents()
            .size(), 2);
        Assert.assertEquals(student3.getTeacher()
            .getStudents()
            .get(2)
            .getRelationshipStudentTeachers()
            .get(1)
            .getTeacher()
            .getStudents()
            .size(), 2);
        Assert.assertEquals(student3.getTeacher()
            .getStudents()
            .get(3)
            .getRelationshipStudentTeachers()
            .get(0)
            .getTeacher()
            .getId()
            .intValue(), 6);
        Assert.assertEquals(student3.getTeacher()
            .getStudents()
            .get(3)
            .getRelationshipStudentTeachers()
            .get(1)
            .getTeacher()
            .getId()
            .intValue(), 2);

        // todo more

    }

    @Test
    public void 一对一关系_一对多关系_无线级关系_builder筛选_快捷用法() {

        Student student3 =
            studentModel.newQuery()
                .firstOrFail()
                .with("teacher.students.teacher.students",
                    builder -> builder.whereIn("id", "3", "2"), record3 -> record3.with("teacher"))
                .toObject();
        System.out.println(student3);

        Assert.assertEquals(student3.getId().intValue(), 1);
        Assert.assertEquals(student3.getTeacherId().intValue(), 6);
        Assert.assertNotNull(student3.getTeacher());
        Assert.assertNull(student3.getRelationshipStudentTeachers());
        Assert.assertNull(student3.getTeacher().getStudent());
        Assert.assertNotNull(student3.getTeacher().getStudents());
        Assert.assertEquals(student3.getTeacher().getStudents().size(), 4);
        // 主要是这行
        Assert.assertEquals(student3.getTeacher().getStudents().get(0).getTeacher().getStudents().size(), 2);
    }


    @Test
    public void 一对一关系_一对多关系_无线级关系_builder筛选_快捷用法2() {

        Student student3 =
            studentModel.newQuery()
                .firstOrFail()
                .with("teacher.students.relationshipStudentTeachers.teacher.students.teacher.students.teacher.students",
                    builder -> builder.whereIn("id", "3", "2"), record3 -> record3.with("teacher"))
                .toObject();
        System.out.println(student3);

        Assert.assertEquals(student3.getId().intValue(), 1);
        Assert.assertEquals(student3.getTeacherId().intValue(), 6);
        Assert.assertNotNull(student3.getTeacher());
        Assert.assertNull(student3.getRelationshipStudentTeachers());
        Assert.assertNull(student3.getTeacher().getStudent());
        Assert.assertNotNull(student3.getTeacher().getStudents());
        Assert.assertEquals(student3.getTeacher().getStudents().size(), 4);
        // 主要是这行
//        Assert.assertEquals(student3.getTeacher().getStudents().get(0).getTeacher().getStudents().size(), 2);
    }

    @Test
    public void builder筛选() {

        Student student =
            studentModel.newQuery()
                .firstOrFail()
                .with("teacher", builder -> builder, record -> record.with("students",
                    builder -> builder.whereColumn("id", "<=", "sex").orderBy("id"), // 小明(1) 小张(2)
                    record1 -> record1.with("teacher", builder -> builder, record2 -> record2.with("students",
                        builder -> builder.whereIn("id", "3", "2"), record3 -> record3.with("teacher")))))
                .toObject();
        System.out.println(student);

        Assert.assertEquals(student.getId().intValue(), 1);
        Assert.assertEquals(student.getTeacherId().intValue(), 6);
        Assert.assertNotNull(student.getTeacher());
        Assert.assertNull(student.getRelationshipStudentTeachers());
        Assert.assertNull(student.getTeacher().getStudent());
        Assert.assertNotNull(student.getTeacher().getStudents());
        Assert.assertEquals(student.getTeacher().getStudents().size(), 2);
        System.out.println(student.getTeacher().getStudents().size());
        Assert.assertEquals(student.getTeacher().getStudents().get(0).getId().intValue(), 1);
        Assert.assertEquals(student.getTeacher().getStudents().get(1).getId().intValue(), 2);
        // 主要是这行
        Assert.assertEquals(student.getTeacher().getStudents().get(0).getTeacher().getStudents().size(), 2);
    }


    @Test
    public void builder筛选2() {

        Student student =
            studentModel.newQuery()
                .firstOrFail()
                .with("teacher", builder -> builder, record -> record.with("students",
                    builder -> builder.whereColumn("id", "<=", "sex").orderBy("id"), // 小明(1) 小张(2)
                    record1 -> record1.with("relationshipStudentTeachers", builder -> builder.orderBy("teacher_id"),
                        record2 -> record2.with("teacher"))))
                .toObject();
        System.out.println(student);

        Assert.assertEquals(student.getId().intValue(), 1);
        Assert.assertEquals(student.getTeacherId().intValue(), 6);
        Assert.assertNotNull(student.getTeacher());
        Assert.assertNull(student.getRelationshipStudentTeachers());
        Assert.assertNull(student.getTeacher().getStudent());
        Assert.assertNotNull(student.getTeacher().getStudents());
        Assert.assertEquals(student.getTeacher().getStudents().size(), 2);
        System.out.println(student.getTeacher().getStudents().size());
        Assert.assertEquals(student.getTeacher().getStudents().get(0).getId().intValue(), 1);
        Assert.assertEquals(student.getTeacher().getStudents().get(1).getId().intValue(), 2);
        // 主要是这行
        Assert.assertEquals(student.getTeacher().getStudents().get(0).getRelationshipStudentTeachers().size(), 2);


        Assert.assertEquals(student.getTeacher()
            .getStudents()
            .get(0)
            .getRelationshipStudentTeachers()
            .get(0)
            .getTeacher()
            .getId()
            .intValue(), 1);
        Assert.assertEquals(student.getTeacher()
            .getStudents()
            .get(0)
            .getRelationshipStudentTeachers()
            .get(1)
            .getTeacher()
            .getId()
            .intValue(), 2);
        Assert.assertEquals(student.getTeacher()
            .getStudents()
            .get(1)
            .getRelationshipStudentTeachers()
            .get(0)
            .getTeacher()
            .getId()
            .intValue(), 1);
        Assert.assertEquals(student.getTeacher()
            .getStudents()
            .get(1)
            .getRelationshipStudentTeachers()
            .get(1)
            .getTeacher()
            .getId()
            .intValue(), 2);

    }

    @Test
    public void 多数据结果_一对一关系_builder筛选1() {
        Set<Object> teacherIds = new HashSet<>();
        teacherIds.add("1");
//        teacherIds.add("2");
//

        List<Student> students = studentModel.newQuery()
            .get()
            .with("teacher", builder -> builder.whereIn("id", teacherIds), record -> record.with(
                "students"))
            .toObjectList();

        for (Student student : students) {
            System.out.println(student);
        }
    }

    @Test
    public void 多数据结果_一对一关系_无线级关系1() {
        List<Student> students = studentModel.newQuery()
            .get()
            .with("teacher", builder -> builder, record -> record
                .with("students")
            )
            .toObjectList();

        for (Student student : students) {
            System.out.println(student);
        }
        Assert.assertNotNull(students.get(0).getTeacher());
        Assert.assertNotNull(students.get(0).getTeacher().getStudents());
        Assert.assertEquals(students.get(0).getTeacher().getStudents().size(), 4);
    }

    @Test
    public void 多数据结果_一对一关系_builder筛选2() {
        Set<Object> teacherIds = new HashSet<>();
        teacherIds.add("1");
//        teacherIds.add("2");
        Set<Object> studentIds = new HashSet<>();
        studentIds.add("5");
        studentIds.add("6");


        List<Student> students = studentModel.newQuery()
            .get()
            .with("teacher", builder -> builder.whereIn("id", teacherIds), record -> record.with(
                "students", builder -> builder.whereIn("id", studentIds)))
//        "students", builder -> builder.whereIn("teacher_id", "5","6")))
            .toObjectList();

        for (Student student : students) {
            System.out.println(student);
        }

    }

    @Test
    public void 多数据结果_一对一关系_无线级关系2() {
        List<Student> students = studentModel.newQuery()
            .get()
            .with("teacher", builder -> builder, record -> record
                .with("students")
            )
            .toObjectList();

        for (Student student : students) {
            System.out.println(student);
        }
        Assert.assertNotNull(students.get(0).getTeacher());
        Assert.assertNotNull(students.get(0).getTeacher().getStudents());
        Assert.assertEquals(students.get(0).getTeacher().getStudents().size(), 4);
    }

    @Test
    public void 多数据结果_一对一关系_builder筛选() {
        Set<Object> teacherIds = new HashSet<>();
        teacherIds.add("1");
//        teacherIds.add("2");
        Set<Object> studentIds = new HashSet<>();
        studentIds.add("5");
        studentIds.add("6");


        Set<Object> teacherIds2 = new HashSet<>();
        teacherIds2.add("1");
        teacherIds2.add("2");
        teacherIds2.add("3");


        Set<Object> studentIds2 = new HashSet<>();
        studentIds2.add("5");
        studentIds2.add("6");
        studentIds2.add("7");
        studentIds2.add("8");

        List<Student> students = studentModel.newQuery()
            .get()
            .with("teacher", builder -> builder.whereIn("id", teacherIds), record -> record.with(
                "students", builder -> builder.whereIn("id", studentIds), record1 -> record1.with(
                    "teacher", builder -> builder.whereIn("id", teacherIds2), record2 -> record2.with("students"
                        , builder -> builder.whereIn("id", studentIds2)))))
            .toObjectList();

        for (Student student : students) {
            System.out.println(student);
        }
    }

    @Test
    public void 多数据结果_一对一关系_无线级关系() {
        List<Student> students = studentModel.newQuery()
            .get()
            .with("teacher", builder -> builder, record -> record
                .with("students", builder -> builder, record1 -> record1
                    .with("teacher", builder -> builder, record2 -> record2.with("students"))
                )
            )
            .toObjectList();

        for (Student student : students) {
            System.out.println(student);
        }
        Assert.assertNotNull(students.get(0).getTeacher());
        Assert.assertNotNull(students.get(0).getTeacher().getStudents());
        Assert.assertEquals(students.get(0).getTeacher().getStudents().size(), 4);
        Assert.assertNotNull(students.get(0).getTeacher().getStudents().get(0).getTeacher());
    }

    @Test
    public void 多数据结果_一对一关系_无线级关系_快捷方式() {
        List<Student> students = studentModel.newQuery()
            .get()
            .with("teacher.students.teacher")
            .toObjectList();

        for (Student student : students) {
            System.out.println(student);
        }
        Assert.assertNotNull(students.get(0).getTeacher());
        Assert.assertNotNull(students.get(0).getTeacher().getStudents());
        Assert.assertEquals(students.get(0).getTeacher().getStudents().size(), 4);
        Assert.assertNotNull(students.get(0).getTeacher().getStudents().get(0).getTeacher());
    }

    @Test
    public void 多数据结果_一对一关系_无线级关系_快捷方式2() {
        List<Student> students = studentModel.newQuery()
            .get()
            .with("teacher", builder -> builder, record -> record.with("students.teacher"))
            .toObjectList();

        for (Student student : students) {
            System.out.println(student);
        }
        Assert.assertNotNull(students.get(0).getTeacher());
        Assert.assertNotNull(students.get(0).getTeacher().getStudents());
        Assert.assertEquals(students.get(0).getTeacher().getStudents().size(), 4);
        Assert.assertNotNull(students.get(0).getTeacher().getStudents().get(0).getTeacher());
    }

    @Test
    public void 一对多关系1() {
        // 声明但不使用
        Record<Teacher, Long> record = teacherModel.newQuery().where("id", "6").firstOrFail();
        Teacher teacherHasMany = record.toObject();
        System.out.println(teacherHasMany);
        Assert.assertNull(teacherHasMany.getStudents());
    }

    @Test
    public void 一对多关系2() {

        // 声明且使用
        Record<Teacher, Long> record2 =
            teacherModel.newQuery().where("id", "6").firstOrFail().with("students");
        Teacher teacherHasMany2 = record2.toObject();
        System.out.println(teacherHasMany2);
        Assert.assertEquals(teacherHasMany2.getStudents().size(), 4);
    }

    @Test
    public void 一对多关系3() {
        // 声明且使用,但无目标数据
        Record<Teacher, Long> record3 =
            teacherModel.newQuery().where("id", "6").firstOrFail().with("students", builder -> builder.where("id",
                "222"));
        Teacher teacherHasMany3 = record3.toObject();
        System.out.println(teacherHasMany3);
        Assert.assertEquals(teacherHasMany3.getStudents().size(), 0);
    }

    @Test
    public void 一对多关系_builder筛选() {
        // 声明且使用
        Record<Teacher, Long> record2 =
            teacherModel.newQuery().where("id", "6").firstOrFail().with("students",
                (builder -> builder.orderBy("id", OrderBy.DESC).where("id", "<=", "2")));
        Teacher teacherHasMany2 = record2.toObject();
        System.out.println(teacherHasMany2);
        Assert.assertEquals(teacherHasMany2.getStudents().size(), 2);
    }

    @Test
    public void 多数据结果_一对多关系_无线级关系() {
        // 声明且使用
        Record<Teacher, Long> record2 =
            teacherModel.newQuery().where("id", "6").firstOrFail().with("students",
                (builder -> builder.orderBy("id", OrderBy.DESC).where("id", "<=", "2")), record -> record.with(
                    "teacher", builder -> builder, record1 -> record1.with("students", builder -> builder,
                        record3 -> record3.with("teacher"))));
        Teacher teacherHasMany2 = record2.toObject();
        System.out.println(teacherHasMany2);
        Assert.assertEquals(teacherHasMany2.getStudents().size(), 2);
    }

    @Test
    public void 多对多关系_中间表() {
        // 声明但不使用
        Student student = studentModel.newQuery()
            .where("id", "1")
            .firstOrFail().with("relationshipStudentTeachers", builder -> builder,
                record1 -> record1.with("teacher", builder -> builder, record -> record.with(
                    "relationshipStudentTeachers", builder -> builder.orderBy("student_id"),
                    record2 -> record2.with("student")))).toObject();

        System.out.println(student);
        Assert.assertEquals(student.getId().intValue(), 1);
        Assert.assertEquals(student.getRelationshipStudentTeachers().size(), 2);
        Assert.assertEquals(student.getRelationshipStudentTeachers().get(0).getStudentId().intValue(), 1);
        Assert.assertEquals(student.getRelationshipStudentTeachers().get(1).getStudentId().intValue(), 1);
        Assert.assertEquals(student.getRelationshipStudentTeachers().get(0).getTeacherId().intValue(), 1);
        Assert.assertEquals(student.getRelationshipStudentTeachers().get(0).getTeacher().getId().intValue(), 1);
        Assert.assertEquals(student.getRelationshipStudentTeachers().get(1).getTeacherId().intValue(), 2);
        Assert.assertEquals(student.getRelationshipStudentTeachers().get(1).getTeacher().getId().intValue(), 2);
        Assert.assertEquals(student.getId().intValue(), 1);

        Assert.assertEquals(
            student.getRelationshipStudentTeachers().get(1).getTeacher().getRelationshipStudentTeachers().size(), 10);

        Assert.assertEquals(student.getRelationshipStudentTeachers()
            .get(1)
            .getTeacher()
            .getRelationshipStudentTeachers()
            .get(0)
            .getStudent()
            .getId()
            .intValue(), 1);
        Assert.assertEquals(student.getRelationshipStudentTeachers()
            .get(1)
            .getTeacher()
            .getRelationshipStudentTeachers()
            .get(9)
            .getStudent()
            .getId()
            .intValue(), 10);


    }

    @Test
    public void 多对多关系_中间表_快捷方式() {
        // 声明但不使用
        Student student = studentModel.newQuery()
            .where("id", "1")
            .firstOrFail().with("relationshipStudentTeachers.teacher.relationshipStudentTeachers",
                builder -> builder.orderBy("student_id"),
                record2 -> record2.with("student")).toObject();

        System.out.println(student);
        Assert.assertEquals(student.getId().intValue(), 1);
        Assert.assertEquals(student.getRelationshipStudentTeachers().size(), 2);
        Assert.assertEquals(student.getRelationshipStudentTeachers().get(0).getStudentId().intValue(), 1);
        Assert.assertEquals(student.getRelationshipStudentTeachers().get(1).getStudentId().intValue(), 1);
        Assert.assertEquals(student.getRelationshipStudentTeachers().get(0).getTeacherId().intValue(), 1);
        Assert.assertEquals(student.getRelationshipStudentTeachers().get(0).getTeacher().getId().intValue(), 1);
        Assert.assertEquals(student.getRelationshipStudentTeachers().get(1).getTeacherId().intValue(), 2);
        Assert.assertEquals(student.getRelationshipStudentTeachers().get(1).getTeacher().getId().intValue(), 2);
        Assert.assertEquals(student.getId().intValue(), 1);

        Assert.assertEquals(
            student.getRelationshipStudentTeachers().get(1).getTeacher().getRelationshipStudentTeachers().size(), 10);

        Assert.assertEquals(student.getRelationshipStudentTeachers()
            .get(1)
            .getTeacher()
            .getRelationshipStudentTeachers()
            .get(0)
            .getStudent()
            .getId()
            .intValue(), 1);
        Assert.assertEquals(student.getRelationshipStudentTeachers()
            .get(1)
            .getTeacher()
            .getRelationshipStudentTeachers()
            .get(9)
            .getStudent()
            .getId()
            .intValue(), 10);


    }

    @Test
    public void 多对多关系_中间表_纯手动() {
        // 主要查询 select * from student where id = 1 limit 1
        Student student = studentModel.newQuery().where("id", "1").firstOrFail().toObject();

        // 初始化老师list, 以便放入老师对象
        student.setTeachersBelongsToMany(new ArrayList<>());

        // 查询老师关系 select * from relationship_student_teacher where student_id = 1
        List<RelationshipStudentTeacher> relationshipStudentTeachers = relationshipStudentTeacherModel.newQuery()
            .where("student_id", student.getId().toString())
            .get()
            .toObjectList();

        for (RelationshipStudentTeacher relationshipStudentTeacher : relationshipStudentTeachers) {

            // 查询老师实体 select * from teacher where id = ? limit 1
            Teacher teacher = teacherModel.newQuery()
                .where("id", relationshipStudentTeacher.getTeacherId().toString())
                .firstOrFail()
                .toObject();

            // 放入老师对象
            student.getTeachersBelongsToMany().add(teacher);

            // 查询学生关系 select * from relationship_student_teacher where teacher_id = ?
            List<String> studentIds = relationshipStudentTeacherModel.newQuery()
                .where("teacher_id",
                    relationshipStudentTeacher.getTeacherId().toString())
                .get()
                .toList(record -> record.toObject().getStudentId().toString());

            // 查询学生列表 select * from student where id in (?)
            List<Student> students = studentModel.newQuery().whereIn("id", studentIds).get().toObjectList();

            // 放入学生对象
            teacher.setStudentsBelongsToMany(students);

            System.out.println("teacherid = " + relationshipStudentTeacher.getTeacherId() + " 的学生如下");
            System.out.println(students);
        }

        assert2(student);
    }

    @Test
    public void 多对多关系_中间表_中间额外数据查询() {
        Student student = studentModel.newQuery()
            .where("id", "1")
            .firstOrFail()
            .with("relationshipStudentTeachers")
            .with("teachersBelongsToMany")
            .toObject();

        System.out.println(student);


        Student student2 = studentModel.newQuery()
            .where("id", "1")
            .firstOrFail()
            .with("teachersBelongsToMany")
            .toObject();
        System.out.println(student2);

    }

    @Test
    public void 多对多关系_不存在关系() {
        // 手动清除
        relationshipStudentTeacherModel.newQuery().whereRaw("1").delete();

        Student student = studentModel.newQuery()
            .where("id", "1")
            .firstOrFail()
            .with("relationshipStudentTeachers")
            .with("teachersBelongsToMany")
            .toObject();


        System.out.println(student);


        Student student2 = studentModel.newQuery()
            .where("id", "1")
            .firstOrFail()
            .with("teachersBelongsToMany")
            .toObject();
        System.out.println(student2);

    }

    @Test
    public void 多对多关系_中间表_BelongsToMany() {
        Student student = studentModel.newQuery()
            .where("id", "1")
            .firstOrFail()
            .with("teachersBelongsToMany", builder -> builder, record -> record.with(
                "studentsBelongsToMany",
                builder -> builder))
            .toObject();
        assert2(student);
    }

    @Test
    public void 多对多关系_中间表_BelongsToMany_快捷方式() {
        // 声明但不使用
        Student student = studentModel.newQuery()
            .where("id", "1")
            .firstOrFail()
            .with("teachersBelongsToMany.studentsBelongsToMany")
            .toObject();

        assert2(student);
    }


    private void assert2(Student student) {
        System.out.println("断言的 student 如下");
        System.out.println(student);

        Assert.assertEquals(student.getId().intValue(), 1);
        Assert.assertEquals(student.getTeachersBelongsToMany().size(), 2);
        Assert.assertEquals(student.getTeachersBelongsToMany().get(0).getId().intValue(), 1);
        Assert.assertEquals(student.getTeachersBelongsToMany().get(1).getId().intValue(), 2);


        Assert.assertEquals(student.getTeachersBelongsToMany().get(0).getStudentsBelongsToMany().size(), 3);
        Assert.assertEquals(student.getTeachersBelongsToMany().get(1).getStudentsBelongsToMany().size(), 10);
        // todo

        System.out.println("teacherid = 1 的学生如下");
        for (Student student1 : student.getTeachersBelongsToMany().get(0).getStudentsBelongsToMany()) {
            System.out.println(student1);
        }

        System.out.println("teacherid = 2 的学生如下");
        for (Student student1 : student.getTeachersBelongsToMany().get(1).getStudentsBelongsToMany()) {
            System.out.println(student1);
        }
        // todo more

    }


    @Test
    public void 关联关系分页_快速分页() {
        Paginate<Student> paginate = studentModel.newQuery().orderBy(Student.ID).with(
            "relationshipStudentTeachers.teacher.relationshipStudentTeachers", builder -> builder.orderBy("student_id"),
            record2 -> record2.with("student")).simplePaginate(1, 3);
        System.out.println(paginate);
        Assert.assertEquals(paginate.getCurrentPage(), 1);
        Assert.assertNotNull(paginate.getFrom());
        Assert.assertNotNull(paginate.getTo());
        Assert.assertEquals(paginate.getFrom().intValue(), 1);
        Assert.assertEquals(paginate.getTo().intValue(), 3);
        Assert.assertNull(paginate.getLastPage());
        Assert.assertNull(paginate.getTotal());

        Student student = paginate.getItemList().get(0);
        通用断言(student);

    }

    @Test
    public void 分页_通用分页() {
        Paginate<Student> paginate =
            studentModel.newQuery().orderBy("id").with(
                "relationshipStudentTeachers.teacher.relationshipStudentTeachers",
                builder -> builder.orderBy("student_id"),
                record2 -> record2.with("student")).paginate(1, 4);
        System.out.println(paginate);
        Assert.assertEquals(paginate.getCurrentPage(), 1);
        Assert.assertNotNull(paginate.getFrom());
        Assert.assertNotNull(paginate.getTo());
        Assert.assertEquals(paginate.getFrom().intValue(), 1);
        Assert.assertEquals(paginate.getTo().intValue(), 4);
        Assert.assertNotNull(paginate.getLastPage());
        Assert.assertNotNull(paginate.getTotal());
        Assert.assertEquals(paginate.getLastPage().intValue(), 3);
        Assert.assertEquals(paginate.getTotal().intValue(), 10);

        Student student = paginate.getItemList().get(0);
        通用断言(student);
    }

    private void 通用断言(Student student) {
        Assert.assertEquals(student.getId().intValue(), 1);
        Assert.assertEquals(student.getRelationshipStudentTeachers().size(), 2);
        Assert.assertEquals(student.getRelationshipStudentTeachers().get(0).getStudentId().intValue(), 1);
        Assert.assertEquals(student.getRelationshipStudentTeachers().get(1).getStudentId().intValue(), 1);
        Assert.assertEquals(student.getRelationshipStudentTeachers().get(0).getTeacherId().intValue(), 1);
        Assert.assertEquals(student.getRelationshipStudentTeachers().get(0).getTeacher().getId().intValue(), 1);
        Assert.assertEquals(student.getRelationshipStudentTeachers().get(1).getTeacherId().intValue(), 2);
        Assert.assertEquals(student.getRelationshipStudentTeachers().get(1).getTeacher().getId().intValue(), 2);
        Assert.assertEquals(student.getId().intValue(), 1);

        Assert.assertEquals(
            student.getRelationshipStudentTeachers().get(1).getTeacher().getRelationshipStudentTeachers().size(), 10);

        Assert.assertEquals(student.getRelationshipStudentTeachers()
            .get(1)
            .getTeacher()
            .getRelationshipStudentTeachers()
            .get(0)
            .getStudent()
            .getId()
            .intValue(), 1);
        Assert.assertEquals(student.getRelationshipStudentTeachers()
            .get(1)
            .getTeacher()
            .getRelationshipStudentTeachers()
            .get(9)
            .getStudent()
            .getId()
            .intValue(), 10);
    }

    @Test
    public void 一对多_关联关系属性类型支持(){
        Teacher teacher = teacherModel.newQuery()
//            .withAggregate(AggregatesType.min, Teacher::getStudents, Student::getAge, builder -> builder.where("sss",231), null)
//            .withMany(Teacher::getStudentArray, builder -> builder.where(Student::getAge, "12"))
            .findOrFail(1)
            .with(Teacher::getStudents)
            .with(Teacher::getStudentArray)
            .with(Teacher::getStudentArrayList)
            // 使用 orWhere 更改查询范围
            .with(Teacher::getStudentLinkedList, builder -> builder.where("id",3).orWhere(builder1 -> builder1.where("id",1)))
            .with(Teacher::getStudentLinkedHashSet)
            .with(Teacher::getStudentSet)
            .toObject();
        System.out.println(teacher);
        Assert.assertNotNull(teacher.getStudents());
        Assert.assertEquals(2, teacher.getStudents().size());

        Assert.assertNotNull(teacher.getStudentArray());
        Assert.assertEquals(2, teacher.getStudentArray().length);

        Assert.assertNotNull(teacher.getStudentArrayList());
        Assert.assertEquals(2, teacher.getStudentArrayList().size());

        // 使用 orWhere 更改查询范围, 但是在数据后续处理中, 没有对应关系键, 所以绑定不成功.
        Assert.assertNotNull(teacher.getStudentLinkedList());
        Assert.assertEquals(0, teacher.getStudentLinkedList().size());

        Assert.assertNotNull(teacher.getStudentLinkedHashSet());
        Assert.assertEquals(2, teacher.getStudentLinkedHashSet().size());

        Assert.assertNotNull(teacher.getStudentSet());
        Assert.assertEquals(2, teacher.getStudentSet().size());
    }

    @Test
    public void 多对多_关联关系属性类型支持(){
        Teacher teacher = teacherModel.findOrFail(1)
            .with(Teacher::getStudentsBelongsToMany)
            .with(Teacher::getStudentsBelongsToManyArray)
            .with(Teacher::getStudentsBelongsToManyLinkedHashSet)
            // 使用 orWhere 更改查询范围
            .with(Teacher::getStudentsBelongsToManySet, builder -> builder.where("id",3).orWhere(builder1 -> builder1.where("id",1)))
            .with(Teacher::getStudentsBelongsToManyArrayList)
            .with(Teacher::getStudentsBelongsToManyLinkedList)
            .toObject();
        System.out.println(teacher);
        Assert.assertNotNull(teacher.getStudentsBelongsToMany());
        Assert.assertEquals(3, teacher.getStudentsBelongsToMany().size());

        Assert.assertNotNull(teacher.getStudentsBelongsToManyArray());
        Assert.assertEquals(3, teacher.getStudentsBelongsToManyArray().length);

        Assert.assertNotNull(teacher.getStudentsBelongsToManyLinkedHashSet());
        Assert.assertEquals(3, teacher.getStudentsBelongsToManyLinkedHashSet().size());

        // 使用 orWhere 更改查询范围
        Assert.assertNotNull(teacher.getStudentsBelongsToManySet());
        Assert.assertEquals(2, teacher.getStudentsBelongsToManySet().size());

        Assert.assertNotNull(teacher.getStudentsBelongsToManyArrayList());
        Assert.assertEquals(3, teacher.getStudentsBelongsToManyArrayList().size());

        Assert.assertNotNull(teacher.getStudentsBelongsToManyLinkedList());
        Assert.assertEquals(3, teacher.getStudentsBelongsToManyLinkedList().size());

    }

    @Test
    public void count_hasOneOrMany(){
        List<Teacher> teachers = teacherModel.newQuery()
            .whereIn(Teacher::getId, 1, 2, 6)
            .orderBy(Teacher::getId)
            .withCount(Teacher::getStudents)
            .get()
            .toObjectList();
        Assert.assertEquals(3, teachers.size());
        Assert.assertEquals(2, teachers.get(0).getStudentsCount().intValue());
        Assert.assertEquals(2, teachers.get(1).getStudentsCount().intValue());
        Assert.assertEquals(4, teachers.get(2).getStudentsCount().intValue());

        Teacher teacher = teacherModel.newQuery().withCount(Teacher::getStudents).findOrFail(1).toObject();
        Assert.assertNotNull(teacher.getStudentsCount());
        Assert.assertEquals(2, teacher.getStudentsCount().intValue());

        Teacher teacher1 = teacherModel.newQuery().withCount(Teacher::getStudents).findOrFail(6).toObject();
        Assert.assertNotNull(teacher1.getStudentsCount());
        Assert.assertEquals(4, teacher1.getStudentsCount().intValue());

        // 指定统计的字段(属性)，以及别名(属性)
        Teacher teacher2 = teacherModel.newQuery().withCount(Teacher::getStudents, Student::getId, Teacher::getStudentsCount).findOrFail(6).toObject();
        Assert.assertNotNull(teacher2.getStudentsCount());
        Assert.assertEquals(4, teacher2.getStudentsCount().intValue());

        // 附带自定义查询
        Teacher teacher3 = teacherModel.newQuery().withCount(Teacher::getStudents, Student::getId, builder -> builder.where(Student::getSex, 2), Teacher::getStudentsCount).findOrFail(6).toObject();
        Assert.assertNotNull(teacher3.getStudentsCount());
        Assert.assertEquals(3, teacher3.getStudentsCount().intValue());

//        // 附带自定义统计
//        Teacher teacher4 = teacherModel.newQuery().withCount(Teacher::getStudents, Student::getId, builder -> builder.group(Student::getSex), Teacher::getStudentsCount).findOrFail(6).toObject();
//        Assert.assertNotNull(teacher4.getStudentsCount());
//        Assert.assertEquals(2, teacher4.getStudentsCount().intValue());
    }

    @Test
    public void count_belongsToMany(){
        List<Teacher> teachers = teacherModel.newQuery()
            .whereIn(Teacher::getId, 1, 6)
            .withCount(Teacher::getStudentsBelongsToMany, Student::getId, Teacher::getStudentsCount)
            .get()
            .toObjectList();
        Assert.assertEquals(2, teachers.size());
        Assert.assertEquals(3, teachers.get(0).getStudentsCount().intValue());
        Assert.assertEquals(6, teachers.get(1).getStudentsCount().intValue());

        Teacher teacher = teacherModel.newQuery().withCount(Teacher::getStudentsBelongsToMany).findOrFail(1).toObject();
        Assert.assertNotNull(teacher.getStudentsBelongsToManyCount());
        Assert.assertEquals(3, teacher.getStudentsBelongsToManyCount().intValue());

        Teacher teacher1 = teacherModel.newQuery().withCount(Teacher::getStudentsBelongsToMany).findOrFail(6).toObject();
        Assert.assertNotNull(teacher1.getStudentsBelongsToManyCount());
        Assert.assertEquals(6, teacher1.getStudentsBelongsToManyCount().intValue());

        // 指定统计的字段(属性)，以及别名(属性)
        Teacher teacher2 = teacherModel.newQuery().withCount(Teacher::getStudentsBelongsToMany, Student::getId, Teacher::getStudentsCount).findOrFail(6).toObject();
        Assert.assertNotNull(teacher2.getStudentsCount());
        Assert.assertEquals(6, teacher2.getStudentsCount().intValue());

        // 附带自定义查询
        Teacher teacher3 = teacherModel.newQuery().withCount(Teacher::getStudentsBelongsToMany, Student::getId, builder -> builder.where(Student::getSex, 2), Teacher::getStudentsCount).findOrFail(6).toObject();
        Assert.assertNotNull(teacher3.getStudentsCount());
        Assert.assertEquals(2, teacher3.getStudentsCount().intValue());

    }

    @Test
    public void notHas_ (){
        RecordList<Student, Long> teacherRecords = studentModel.newQuery()
            .whereNotExists(builder -> builder.from("teacher").whereColumn("student.teacher_id", "teacher.id"))
            .get();
        List<Student> students = teacherRecords.toObjectList();
        System.out.println(students);
    }

    @Test
    public void notHas_1 (){
        RecordList<Student, Long> teacherRecords = studentModel.newQuery()
            .whereNotExists(builder -> builder.from("relationship_student_teacher").whereColumn("student.id", "relationship_student_teacher.student_id"))
            .get();
        List<Student> students = teacherRecords.toObjectList();
        System.out.println(students);
    }
}
