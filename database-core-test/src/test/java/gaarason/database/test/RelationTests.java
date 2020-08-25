package gaarason.database.test;

import gaarason.database.connections.ProxyDataSource;
import gaarason.database.contracts.function.GenerateSqlPart;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.enums.OrderBy;
import gaarason.database.query.Builder;
import gaarason.database.test.parent.BaseTests;
import gaarason.database.test.relation.data.model.RelationshipStudentTeacherModel;
import gaarason.database.test.relation.data.model.StudentModel;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
public class RelationTests extends BaseTests {

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    private static StudentModel studentModel = new StudentModel();

    private static TeacherModel teacherModel = new TeacherModel();

    private static RelationshipStudentTeacherModel relationshipStudentTeacherModel =
        new RelationshipStudentTeacherModel();

//    private static StudentModel studentModel = new StudentModel();
//
//    private static TeacherModel teacherModel = new TeacherModel();
//
//    private static StudentBelongsToManyModel studentBelongsToManyModel = new StudentBelongsToManyModel();
//
//    private static StudentBelongsToModel studentBelongsToModel = new StudentBelongsToModel();

    protected List<DataSource> getDataSourceList() {
        ProxyDataSource proxyDataSource = studentModel.getProxyDataSource();
        return proxyDataSource.getMasterDataSourceList();
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
    }

    @Test
    public void 一对一关系_子关系不存在() {
        // 声明且使用
        Student student2 =
            studentModel.newQuery().firstOrFail().with("teacher", builder -> builder.where("id", "99"),
                record -> record.with("students", builder -> builder, record1 -> record1.with("teacher"))).toObject();
        System.out.println(student2);
        Assert.assertNull(student2.getTeacher());
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
    public void 一对一关系_一对多关系_快捷用法() {
        Student student =
            studentModel.newQuery().where("id", "1")
                .firstOrFail()
                .with("teacher.student", builder -> builder.whereIn("id", "1", "2", "3"))
                .with("relationshipStudentTeachers", builder -> builder.whereNotIn("id", "13"))
                .toObject();
        System.out.println(student);
//        Assert.assertNotNull(student.getTeacher());
//        Assert.assertEquals(student.getTeacher().getId().intValue(), 6);
//        Assert.assertNotNull(student.getRelationshipStudentTeachers());
//        Assert.assertEquals(student.getRelationshipStudentTeachers().get(0).getStudentId().intValue(), 1);
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
        Record<Teacher, Integer> record         = teacherModel.newQuery().where("id", "6").firstOrFail();
        Teacher                  teacherHasMany = record.toObject();
        System.out.println(teacherHasMany);
        Assert.assertNull(teacherHasMany.getStudents());
    }

    @Test
    public void 一对多关系2() {

        // 声明且使用
        Record<Teacher, Integer> record2 =
            teacherModel.newQuery().where("id", "6").firstOrFail().with("students");
        Teacher teacherHasMany2 = record2.toObject();
        System.out.println(teacherHasMany2);
        Assert.assertEquals(teacherHasMany2.getStudents().size(), 4);
    }

    @Test
    public void 一对多关系3() {
        // 声明且使用,但无目标数据
        Record<Teacher, Integer> record3 =
            teacherModel.newQuery().where("id", "6").firstOrFail().with("students", builder -> builder.where("id",
                "222"));
        Teacher teacherHasMany3 = record3.toObject();
        System.out.println(teacherHasMany3);
        Assert.assertEquals(teacherHasMany3.getStudents().size(), 0);
    }

    @Test
    public void 一对多关系_builder筛选() {
        // 声明且使用
        Record<Teacher, Integer> record2 =
            teacherModel.newQuery().where("id", "6").firstOrFail().with("students",
                (builder -> builder.orderBy("id", OrderBy.DESC).where("id", "<=", "2")));
        Teacher teacherHasMany2 = record2.toObject();
        System.out.println(teacherHasMany2);
        Assert.assertEquals(teacherHasMany2.getStudents().size(), 2);
    }

    @Test
    public void 多数据结果_一对多关系_无线级关系() {
        // 声明且使用
        Record<Teacher, Integer> record2 =
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
            List<Object> studentIds = relationshipStudentTeacherModel.newQuery()
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
    public void 多对多关系_中间表_BelongsToMany() {
        // 声明但不使用
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


//
//    @Test
//    public void 多对多关系_builder筛选() {
//        Record<StudentBelongsToMany, Long> record2 = studentBelongsToManyModel.newQuery()
//            .where("id", "6")
//            .firstOrFail();
//        StudentBelongsToMany studentBelongsToMany2 = record2.with("teachers", builder -> builder.limit(1)).toObject();
//        System.out.println(studentBelongsToMany2);
//        Assert.assertEquals(studentBelongsToMany2.getTeachers().size(), 1);
//    }
//
//    @Test
//    public void 多数据结果_多对多关系_builder筛选() {
//        RecordList<StudentBelongsToMany, Long> records = studentBelongsToManyModel.newQuery()
//            .where("id", "<", "6")
//            .get();
//        List<StudentBelongsToMany> students = records.with("teachers").toObjectList();
//
//        for (StudentBelongsToMany student : students) {
//            System.out.println(student);
//        }
//        Assert.assertEquals(students.size(), 5);
//        Assert.assertEquals(students.get(0).getTeachers().get(0).getId().intValue(), 1);
//        Assert.assertEquals(students.get(0).getTeachers().get(1).getId().intValue(), 2);
//        Assert.assertEquals(students.get(1).getTeachers().get(0).getId().intValue(), 1);
//        Assert.assertEquals(students.get(1).getTeachers().get(1).getId().intValue(), 2);
//        Assert.assertEquals(students.get(2).getTeachers().get(0).getId().intValue(), 1);
//        Assert.assertEquals(students.get(2).getTeachers().get(1).getId().intValue(), 2);
//        Assert.assertEquals(students.get(3).getTeachers().get(0).getId().intValue(), 2);
//        Assert.assertEquals(students.get(3).getTeachers().get(1).getId().intValue(), 6);
//        Assert.assertEquals(students.get(4).getTeachers().get(0).getId().intValue(), 2);
//        Assert.assertEquals(students.get(4).getTeachers().get(1).getId().intValue(), 6);
//    }
//
//    @Test
//    public void 反一对一关系() {
//        // 声明但不使用
//        StudentBelongsTo studentBelongsTo = studentBelongsToModel.newQuery().firstOrFail().toObject();
//        System.out.println(studentBelongsTo);
//        System.out.println(studentBelongsTo.getTeacher());
//        Assert.assertNull(studentBelongsTo.getTeacher());
//
//        // 声明且使用
//        StudentBelongsTo studentBelongsTo2 = studentBelongsToModel.newQuery().firstOrFail().with("teacher").toObject();
//        System.out.println(studentBelongsTo2);
//        System.out.println(studentBelongsTo2.getTeacher());
//        Assert.assertEquals((long) studentBelongsTo2.getTeacher().getId(), 6);
//    }
//
//    @Test
//    public void 反一对一关系_builder筛选() {
//        StudentBelongsTo studentBelongsTo2 =
//            studentBelongsToModel.newQuery()
//                .firstOrFail()
//                .with("teacher", builder -> builder.select("id", "name").select("id", "name"))
//                .toObject();
//        System.out.println(studentBelongsTo2);
//        System.out.println(studentBelongsTo2.getTeacher());
//        Assert.assertEquals((long) studentBelongsTo2.getTeacher().getId(), 6);
//        Assert.assertNull(studentBelongsTo2.getTeacher().getAge());
//    }
//
//    @Test
//    public void 多数据结果_反一对一关系() {
//        List<StudentBelongsTo> studentList =
//            studentBelongsToModel.newQuery().get().with("teacher",
//                builder -> builder.select("id", "name")).toObjectList();
//
//        for (StudentBelongsTo studentBelongsTo : studentList) {
//            System.out.println(studentBelongsTo);
//
//        }
//        Assert.assertEquals((long) studentList.get(0).getTeacher().getId(), 6);
//        Assert.assertNull(studentList.get(0).getTeacher().getAge());
//    }

}
