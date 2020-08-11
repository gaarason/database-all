package gaarason.database.test;

import gaarason.database.connections.ProxyDataSource;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.enums.OrderBy;
import gaarason.database.test.parent.BaseTests;
import gaarason.database.test.relation.data.model.RelationshipStudentTeacherModel;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
public class RelationTests extends BaseTests {

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
        System.out.println(student.getTeacher());
        Assert.assertNull(student.getTeacher());

        // 声明且使用
        Student student2 = studentModel.newQuery().firstOrFail().with("teacher").toObject();
        System.out.println(student2);
        Assert.assertNotNull(student2.getTeacher());
        Assert.assertEquals((long) student2.getTeacher().getId(), 6);
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
    }

    @Test
    public void 一对一关系_无线级关系() {
        // select * from student limit 1
        // select * from teacher where id in (?)
        // select * from student where id in (? ?)
        Student student2 =
            studentModel.newQuery()
                .firstOrFail()
                .with("teacher", builder -> builder, record -> record.with("students", builder -> builder))
                .toObject();
        System.out.println(student2);
//        Assert.assertNull(student2.getTeacher());
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
                    "teacher", builder -> builder.whereIn("id", teacherIds2), record2 -> record2.with("students!!!!!"
                        , builder -> builder.whereIn("id", studentIds2)))))
            .toObjectList();


//        List<Student> students = studentModel.newQuery()
//            .get()
//            .with("teacher", builder -> builder, record -> record.with("students"))
////                record1 -> record1.with("teacher")))
//            .toObjectList();


        for (Student student : students) {
            System.out.println(student);
        }
    }

    @Test
    public void 多数据结果_一对一关系_无线级关系() {
        List<Student> students = studentModel.newQuery()
            .get()
            .with("teacher", builder -> builder, record -> record
                .with("students"
                    , builder -> builder,
                    record1 -> record1.with("teacher")
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
    public void 一对多关系() {
        // 声明但不使用
        Record<Teacher, Integer> record         = teacherModel.newQuery().where("id", "6").firstOrFail();
        Teacher                  teacherHasMany = record.toObject();
        System.out.println(teacherHasMany);
        Assert.assertNull(teacherHasMany.getStudents());


        // 声明且使用
        Record<Teacher, Integer> record2 =
            teacherModel.newQuery().where("id", "6").firstOrFail().with("students");
        Teacher teacherHasMany2 = record2.toObject();
        System.out.println(teacherHasMany2);
        Assert.assertEquals(teacherHasMany2.getStudents().size(), 4);
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

//    @Test
//    public void 多对多关系() {
//        // 声明但不使用
//        Record<StudentBelongsToMany, Long> record = studentBelongsToManyModel.newQuery()
//            .where("id", "6")
//            .firstOrFail();
//        StudentBelongsToMany studentBelongsToMany = record.toObject();
//        System.out.println(studentBelongsToMany);
//        Assert.assertNull(studentBelongsToMany.getTeachers());
//
//        // 声明且使用
//        Record<StudentBelongsToMany, Long> record2 = studentBelongsToManyModel.newQuery()
//            .where("id", "6")
//            .firstOrFail();
//        StudentBelongsToMany studentBelongsToMany2 = record2.with("teachers").toObject();
//        System.out.println(studentBelongsToMany2);
//        Assert.assertEquals(studentBelongsToMany2.getTeachers().size(), 2);
//    }
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
