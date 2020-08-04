package gaarason.database.test;

import gaarason.database.connections.ProxyDataSource;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.RecordList;
import gaarason.database.eloquent.enums.OrderBy;
import gaarason.database.test.parent.BaseTests;
import gaarason.database.test.relation.data.model.*;
import gaarason.database.test.relation.data.pojo.StudentBelongsTo;
import gaarason.database.test.relation.data.pojo.StudentBelongsToMany;
import gaarason.database.test.relation.data.pojo.StudentHasOne;
import gaarason.database.test.relation.data.pojo.TeacherHasMany;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.sql.DataSource;
import java.util.List;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
public class RelationTests extends BaseTests {

    private static StudentModel studentModel = new StudentModel();

    private static StudentHasOneModel studentHasOneModel = new StudentHasOneModel();

    private static TeacherHasManyModel teacherHasManyModel = new TeacherHasManyModel();

    private static StudentBelongsToManyModel studentBelongsToManyModel = new StudentBelongsToManyModel();

    private static StudentBelongsToModel studentBelongsToModel = new StudentBelongsToModel();

    protected List<DataSource> getDataSourceList() {
        ProxyDataSource proxyDataSource = studentModel.getProxyDataSource();
        return proxyDataSource.getMasterDataSourceList();
    }

    @Test
    public void 一对一关系() {
        // 声明但不使用
        StudentHasOne studentHasOne = studentHasOneModel.newQuery().firstOrFail().toObject();
        System.out.println(studentHasOne);
        System.out.println(studentHasOne.getTeacher());
        Assert.assertNull(studentHasOne.getTeacher());

        // 声明且使用
        StudentHasOne studentHasOne2 = studentHasOneModel.newQuery().firstOrFail().with("teacher").toObject();
        System.out.println(studentHasOne2);
        Assert.assertNotNull(studentHasOne2.getTeacher());
        Assert.assertEquals((long) studentHasOne2.getTeacher().getId(), 6);
    }

    @Test
    public void 一对一关系_builder筛选() {
        StudentHasOne studentHasOne2 =
            studentHasOneModel.newQuery()
                .firstOrFail()
                .with("teacher", (builder -> builder.where("id", "!=", "6")))
                .toObject();
        System.out.println(studentHasOne2);
        Assert.assertNull(studentHasOne2.getTeacher());
    }

    @Test
    public void 一对一关系_无线级关系() {
        // select * from student limit 1
        // select * from teacher where id in (?)
        // select * from student where id in (? ?)
        StudentHasOne studentHasOne2 =
            studentHasOneModel.newQuery()
                .firstOrFail()
                .with("teacher", builder -> builder, record -> record.with("students", builder -> builder))
                .toObject();
        System.out.println(studentHasOne2);
//        Assert.assertNull(studentHasOne2.getTeacher());
    }

    @Test
    public void 多数据结果_一对一关系_builder筛选() {
        List<StudentHasOne> students = studentHasOneModel.newQuery()
            .get()
            .with("teacher", builder -> builder, record -> record.with("students", builder -> builder,
                record1 -> record1.with("teacher")))
            .toObjectList();

        for (StudentHasOne student : students) {
            System.out.println(student);
        }
    }

    @Test
    public void 多数据结果_一对一关系_无线级关系() {
        List<StudentHasOne> students = studentHasOneModel.newQuery()
            .get()
            .with("teacher", builder -> builder, record -> record
                .with("students"
                    , builder -> builder,
                    record1 -> record1.with("teacher")
                )
            )
            .toObjectList();

        for (StudentHasOne student : students) {
            System.out.println(student);
        }
        Assert.assertNotNull(students.get(0).getTeacher());
        Assert.assertNotNull(students.get(0).getTeacher().getStudents());
        Assert.assertEquals(students.get(0).getTeacher().getStudents().size(), 4);
        Assert.assertNotNull(students.get(0).getTeacher().getStudents().get(0).getTeacher());

    }

    @Test
    public void 一对多关系() {
        // 声明但不使用
        Record<TeacherHasMany, Integer> record         = teacherHasManyModel.newQuery().where("id", "6").firstOrFail();
        TeacherHasMany                  teacherHasMany = record.toObject();
        System.out.println(teacherHasMany);
        Assert.assertNull(teacherHasMany.getStudents());


        // 声明且使用
        Record<TeacherHasMany, Integer> record2 =
            teacherHasManyModel.newQuery().where("id", "6").firstOrFail().with("students");
        TeacherHasMany teacherHasMany2 = record2.toObject();
        System.out.println(teacherHasMany2);
        Assert.assertEquals(teacherHasMany2.getStudents().size(), 4);
    }


    @Test
    public void 一对多关系_builder筛选() {
        // 声明且使用
        Record<TeacherHasMany, Integer> record2 =
            teacherHasManyModel.newQuery().where("id", "6").firstOrFail().with("students",
                (builder -> builder.orderBy("id", OrderBy.DESC).where("id", "<=", "2")));
        TeacherHasMany teacherHasMany2 = record2.toObject();
        System.out.println(teacherHasMany2);
        Assert.assertEquals(teacherHasMany2.getStudents().size(), 2);
    }

    @Test
    public void 多数据结果_一对多关系_无线级关系() {
        // 声明且使用
        Record<TeacherHasMany, Integer> record2 =
            teacherHasManyModel.newQuery().where("id", "6").firstOrFail().with("students",
                (builder -> builder.orderBy("id", OrderBy.DESC).where("id", "<=", "2")), record -> record.with(
                    "teacher", builder -> builder, record1 -> record1.with("students", builder -> builder,
                        record3 -> record3.with("teacher"))));
        TeacherHasMany teacherHasMany2 = record2.toObject();
        System.out.println(teacherHasMany2);
        Assert.assertEquals(teacherHasMany2.getStudents().size(), 2);
    }

    @Test
    public void 多对多关系() {
        // 声明但不使用
        Record<StudentBelongsToMany, Long> record = studentBelongsToManyModel.newQuery()
            .where("id", "6")
            .firstOrFail();
        StudentBelongsToMany studentBelongsToMany = record.toObject();
        System.out.println(studentBelongsToMany);
        Assert.assertNull(studentBelongsToMany.getTeachers());

        // 声明且使用
        Record<StudentBelongsToMany, Long> record2 = studentBelongsToManyModel.newQuery()
            .where("id", "6")
            .firstOrFail();
        StudentBelongsToMany studentBelongsToMany2 = record2.with("teachers").toObject();
        System.out.println(studentBelongsToMany2);
        Assert.assertEquals(studentBelongsToMany2.getTeachers().size(), 2);
    }

    @Test
    public void 多对多关系_builder筛选() {
        Record<StudentBelongsToMany, Long> record2 = studentBelongsToManyModel.newQuery()
            .where("id", "6")
            .firstOrFail();
        StudentBelongsToMany studentBelongsToMany2 = record2.with("teachers", builder -> builder.limit(1)).toObject();
        System.out.println(studentBelongsToMany2);
        Assert.assertEquals(studentBelongsToMany2.getTeachers().size(), 1);
    }

    @Test
    public void 多数据结果_多对多关系_builder筛选() {
        RecordList<StudentBelongsToMany, Long> records = studentBelongsToManyModel.newQuery()
            .where("id", "<", "6")
            .get();
        List<StudentBelongsToMany> students = records.with("teachers").toObjectList();

        for (StudentBelongsToMany student : students) {
            System.out.println(student);
        }
        Assert.assertEquals(students.size(), 5);
        Assert.assertEquals(students.get(0).getTeachers().get(0).getId().intValue(), 1);
        Assert.assertEquals(students.get(0).getTeachers().get(1).getId().intValue(), 2);
        Assert.assertEquals(students.get(1).getTeachers().get(0).getId().intValue(), 1);
        Assert.assertEquals(students.get(1).getTeachers().get(1).getId().intValue(), 2);
        Assert.assertEquals(students.get(2).getTeachers().get(0).getId().intValue(), 1);
        Assert.assertEquals(students.get(2).getTeachers().get(1).getId().intValue(), 2);
        Assert.assertEquals(students.get(3).getTeachers().get(0).getId().intValue(), 2);
        Assert.assertEquals(students.get(3).getTeachers().get(1).getId().intValue(), 6);
        Assert.assertEquals(students.get(4).getTeachers().get(0).getId().intValue(), 2);
        Assert.assertEquals(students.get(4).getTeachers().get(1).getId().intValue(), 6);
    }

    @Test
    public void 反一对一关系() {
        // 声明但不使用
        StudentBelongsTo studentBelongsTo = studentBelongsToModel.newQuery().firstOrFail().toObject();
        System.out.println(studentBelongsTo);
        System.out.println(studentBelongsTo.getTeacher());
        Assert.assertNull(studentBelongsTo.getTeacher());

        // 声明且使用
        StudentBelongsTo studentBelongsTo2 = studentBelongsToModel.newQuery().firstOrFail().with("teacher").toObject();
        System.out.println(studentBelongsTo2);
        System.out.println(studentBelongsTo2.getTeacher());
        Assert.assertEquals((long) studentBelongsTo2.getTeacher().getId(), 6);
    }

    @Test
    public void 反一对一关系_builder筛选() {
        StudentBelongsTo studentBelongsTo2 =
            studentBelongsToModel.newQuery()
                .firstOrFail()
                .with("teacher", builder -> builder.select("id", "name").select("id", "name"))
                .toObject();
        System.out.println(studentBelongsTo2);
        System.out.println(studentBelongsTo2.getTeacher());
        Assert.assertEquals((long) studentBelongsTo2.getTeacher().getId(), 6);
        Assert.assertNull(studentBelongsTo2.getTeacher().getAge());
    }

    @Test
    public void 多数据结果_反一对一关系() {
        List<StudentBelongsTo> studentList =
            studentBelongsToModel.newQuery().get().with("teacher",
                builder -> builder.select("id", "name")).toObjectList();

        for (StudentBelongsTo studentBelongsTo : studentList) {
            System.out.println(studentBelongsTo);

        }
        Assert.assertEquals((long) studentList.get(0).getTeacher().getId(), 6);
        Assert.assertNull(studentList.get(0).getTeacher().getAge());
    }

}
