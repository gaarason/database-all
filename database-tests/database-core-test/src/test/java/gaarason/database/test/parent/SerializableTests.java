package gaarason.database.test.parent;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.test.models.relation.model.StudentModel;
import gaarason.database.test.models.relation.pojo.Student;
import gaarason.database.test.parent.base.BaseTests;
import gaarason.database.util.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Collections;
import java.util.List;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
abstract public class SerializableTests extends BaseTests {

    private static final StudentModel studentModel = new StudentModel();

    @Override
    protected GaarasonDataSource getGaarasonDataSource() {
        return studentModel.getGaarasonDataSource();
    }

    @Override
    protected List<TABLE> getInitTables() {
        return Collections.singletonList(TABLE.student);
    }

    @Test
    public void Record序列化_deepCopy() {
        Record<Student, Long> record = studentModel.findOrFail(2);

        Record<Student, Long> recordCopy = ObjectUtils.deepCopy(record);
        System.out.println(recordCopy);

        Student object = recordCopy.toObject();
        Assert.assertEquals(2, object.getId().intValue());
    }

    @Test
    public void RecordList序列化_deepCopy() {
        RecordList<Student, Long> records = studentModel.newQuery().get();
        Assert.assertEquals(10, records.size());
        List<Student> students = records.toObjectList();
        System.out.println(students);
        Assert.assertNotNull(students);
        System.out.println(students.size());
        Assert.assertEquals(10, students.size());


        RecordList<Student, Long> recordsCopy = ObjectUtils.deepCopy(records);
        System.out.println(recordsCopy);
        Assert.assertEquals(10, recordsCopy.size());

        List<Student> object = recordsCopy.toObjectList();
        System.out.println(object);
        Assert.assertNotNull(object);
        System.out.println(object.size());
        Assert.assertEquals(10, object.size());
    }

    @Test
    public void Builder序列化_deepCopy() {
        Builder<?, Student, Long> builder = studentModel.newQuery().where(Student::getId, 4);
        Builder<?, Student, Long> deepCopyBuilder = ObjectUtils.deepCopy(builder);
        Student student = deepCopyBuilder.firstOrFail().toObject();
        Assert.assertEquals(4, student.getId().intValue());
    }

    @Test
    public void Record序列化_serializeToString() {
        Student student1 = new Student();
        student1.setAge(12);

        Record<Student, Long> record = studentModel.findOrFail(2).with("teachersBelongsToMany", b -> {
            return b.limit(student1.getAge());
        });

        String serialize = record.serializeToString();
        System.out.println(serialize);
        Assert.assertFalse(ObjectUtils.isEmpty(serialize));
        System.out.println(serialize.length());


        Student student = record.toObject();
        Assert.assertNotNull(student.getTeachersBelongsToMany());

        Record<Student, Long> recordCopy = Record.deserialize(serialize);

        System.out.println(recordCopy);

        Student object = recordCopy.toObject();
        System.out.println(recordCopy);
        System.out.println(recordCopy.toString().length());
        System.out.println(object);
        System.out.println(object.toString().length());
        Assert.assertEquals(2, object.getId().intValue());
        Assert.assertNotNull(object.getTeachersBelongsToMany());
    }

    @Test
    public void RecordList序列化_serializeToString() {
        Student student1 = new Student();
        student1.setAge(3);

        RecordList<Student, Long> records = studentModel.newQuery().with("teachersBelongsToMany", b -> {
            return b.limit(student1.getAge());
        }).get();

        String serialize = records.serializeToString();
        System.out.println(serialize);
        Assert.assertFalse(ObjectUtils.isEmpty(serialize));
        System.out.println(serialize.length());


        List<Student> student = records.toObjectList();
        Assert.assertFalse(ObjectUtils.isEmpty(student));
        Assert.assertNotNull(student.get(0).getTeachersBelongsToMany());

        RecordList<Student, Long> recordsCopy = RecordList.deserialize(serialize);

        System.out.println(recordsCopy);

        List<Student> objects = recordsCopy.toObjectList();
        System.out.println(recordsCopy);
        System.out.println(recordsCopy.toString().length());
        System.out.println(objects);
        System.out.println(objects.toString().length());
        Assert.assertFalse(ObjectUtils.isEmpty(objects));
        Assert.assertNotNull(objects.get(0).getTeachersBelongsToMany());
    }

    @Test
    public void builder序列化_serializeToString() {
        Student student1 = new Student();
        student1.setAge(3);

        Builder<?, Student, Long> builder = studentModel.newQuery().with("teachersBelongsToMany", b -> {
            return b.limit(student1.getAge());
        });

        String serialize = builder.serializeToString();
        System.out.println(serialize);
        Assert.assertFalse(ObjectUtils.isEmpty(serialize));
        System.out.println(serialize.length());


        List<Student> student = builder.get().toObjectList();
        Assert.assertFalse(ObjectUtils.isEmpty(student));
        Assert.assertNotNull(student.get(0).getTeachersBelongsToMany());

        Builder<?, Student, Long> builderCopy = Builder.deserialize(serialize);

        System.out.println(builderCopy);

        List<Student> objects = builderCopy.get().toObjectList();
        System.out.println(objects);
        System.out.println(objects.toString().length());
        Assert.assertFalse(ObjectUtils.isEmpty(objects));
        Assert.assertNotNull(objects.get(0).getTeachersBelongsToMany());
    }

    @Test
    public void Record序列化_serialize() {
        Record<Student, Long> record = studentModel.findOrFail(2);

        byte[] serialize = record.serialize();
        Assert.assertFalse(ObjectUtils.isEmpty(serialize));
        System.out.println(serialize.length);
        System.out.println(serialize);

        Record<Student, Integer> recordCopy = Record.deserialize(serialize);

        System.out.println(recordCopy);

        Student object = recordCopy.toObject();
        Assert.assertEquals(2, object.getId().intValue());
    }

    /**
     * @see #Record序列化_serializeToString
     */
    @Test
    public void Record反序列化() {
        String s = "rO0ABXNyACVnYWFyYXNvbi5kYXRhYmFzZS5lbG9xdWVudC5SZWNvcmRCZWFuAAAAAAAAAAEMAAB4cHdDAAZteXNxbDIAOWdhYXJhc29uLmRhdGFiYXNlLnRlc3QubW9kZWxzLnJlbGF0aW9uLm1vZGVsLlN0dWRlbnRNb2RlbHNyABFqYXZhLnV0aWwuSGFzaE1hcAUH2sHDFmDRAwACRgAKbG9hZEZhY3RvckkACXRocmVzaG9sZHhwP0AAAAAAAAx3CAAAABAAAAAIdAAKaXNfZGVsZXRlZHNyABFqYXZhLmxhbmcuQm9vbGVhbs0gcoDVnPruAgABWgAFdmFsdWV4cAB0AAp1cGRhdGVkX2F0c3IADmphdmEudXRpbC5EYXRlaGqBAUtZdBkDAAB4cHcIAAABKDAoPNh4dAAKdGVhY2hlcl9pZHNyAA5qYXZhLmxhbmcuTG9uZzuL5JDMjyPfAgABSgAFdmFsdWV4cgAQamF2YS5sYW5nLk51bWJlcoaslR0LlOCLAgAAeHAAAAAAAAAABnQAA3NleHNyABFqYXZhLmxhbmcuSW50ZWdlchLioKT3gYc4AgABSQAFdmFsdWV4cQB-AAwAAAACdAAEbmFtZXQABuWwj-W8oHQACmNyZWF0ZWRfYXRzcQB-AAh3CAAAASAD1cb4eHQAAmlkc3EAfgALAAAAAAAAAAJ0AANhZ2VzcQB-AA8AAAALeHd6AHhzZWxlY3QgYG5hbWVgLGBhZ2VgLGBzZXhgLGB0ZWFjaGVyX2lkYCxgaXNfZGVsZXRlZGAsYGNyZWF0ZWRfYXRgLGB1cGRhdGVkX2F0YCxgaWRgIGZyb20gc3R1ZGVudCB3aGVyZSBgaWRgPSA_ICBsaW1pdCAgPyBzcQB-AAI_QAAAAAAADHcIAAAAEAAAAAF0ABV0ZWFjaGVyc0JlbG9uZ3NUb01hbnlzcgAzZ2FhcmFzb24uZGF0YWJhc2UuY29udHJhY3QuZWxvcXVlbnQuUmVjb3JkJFJlbGF0aW9uAAAAAAAAAAECAAVaABFyZWxhdGlvbk9wZXJhdGlvbkwADWN1c3RvbUJ1aWxkZXJ0ADRMZ2FhcmFzb24vZGF0YWJhc2UvY29udHJhY3QvZnVuY3Rpb24vQnVpbGRlcldyYXBwZXI7TAAQb3BlcmF0aW9uQnVpbGRlcnEAfgAcTAANcmVjb3JkV3JhcHBlcnQAM0xnYWFyYXNvbi9kYXRhYmFzZS9jb250cmFjdC9mdW5jdGlvbi9SZWNvcmRXcmFwcGVyO0wAEXJlbGF0aW9uRmllbGROYW1ldAASTGphdmEvbGFuZy9TdHJpbmc7eHAAc3IAIWphdmEubGFuZy5pbnZva2UuU2VyaWFsaXplZExhbWJkYW9h0JQsKTaFAgAKSQAOaW1wbE1ldGhvZEtpbmRbAAxjYXB0dXJlZEFyZ3N0ABNbTGphdmEvbGFuZy9PYmplY3Q7TAAOY2FwdHVyaW5nQ2xhc3N0ABFMamF2YS9sYW5nL0NsYXNzO0wAGGZ1bmN0aW9uYWxJbnRlcmZhY2VDbGFzc3EAfgAeTAAdZnVuY3Rpb25hbEludGVyZmFjZU1ldGhvZE5hbWVxAH4AHkwAImZ1bmN0aW9uYWxJbnRlcmZhY2VNZXRob2RTaWduYXR1cmVxAH4AHkwACWltcGxDbGFzc3EAfgAeTAAOaW1wbE1ldGhvZE5hbWVxAH4AHkwAE2ltcGxNZXRob2RTaWduYXR1cmVxAH4AHkwAFmluc3RhbnRpYXRlZE1ldGhvZFR5cGVxAH4AHnhwAAAABnVyABNbTGphdmEubGFuZy5PYmplY3Q7kM5YnxBzKWwCAAB4cAAAAAFzcgAzZ2FhcmFzb24uZGF0YWJhc2UudGVzdC5tb2RlbHMucmVsYXRpb24ucG9qby5TdHVkZW50lNMw2KUPFh0CAAtMAANhZ2V0ABNMamF2YS9sYW5nL0ludGVnZXI7TAAJY3JlYXRlZEF0dAAQTGphdmEvdXRpbC9EYXRlO0wACWlzRGVsZXRlZHQAE0xqYXZhL2xhbmcvQm9vbGVhbjtMAARuYW1lcQB-AB5MABpyZWxhdGlvbnNoaXBTdHVkZW50VGVhY2hlcnQASExnYWFyYXNvbi9kYXRhYmFzZS90ZXN0L21vZGVscy9yZWxhdGlvbi9wb2pvL1JlbGF0aW9uc2hpcFN0dWRlbnRUZWFjaGVyO0wAG3JlbGF0aW9uc2hpcFN0dWRlbnRUZWFjaGVyc3QAEExqYXZhL3V0aWwvTGlzdDtMAANzZXhxAH4AJ0wAB3RlYWNoZXJ0ADVMZ2FhcmFzb24vZGF0YWJhc2UvdGVzdC9tb2RlbHMvcmVsYXRpb24vcG9qby9UZWFjaGVyO0wACXRlYWNoZXJJZHQAEExqYXZhL2xhbmcvTG9uZztMABV0ZWFjaGVyc0JlbG9uZ3NUb01hbnlxAH4AK0wACXVwZGF0ZWRBdHEAfgAoeHIAO2dhYXJhc29uLmRhdGFiYXNlLnRlc3QubW9kZWxzLnJlbGF0aW9uLnBvam8uYmFzZS5CYXNlRW50aXR5AAAAAAAAAAECAAFMAAJpZHEAfgAteHBwc3EAfgAPAAAADHBwcHBwcHBwcHB2cgAvZ2FhcmFzb24uZGF0YWJhc2UudGVzdC5wYXJlbnQuU2VyaWFsaXphYmxlVGVzdHMAAAAAAAAAAAAAAHhwdAAyZ2FhcmFzb24vZGF0YWJhc2UvY29udHJhY3QvZnVuY3Rpb24vQnVpbGRlcldyYXBwZXJ0AAdleGVjdXRldABcKExnYWFyYXNvbi9kYXRhYmFzZS9jb250cmFjdC9lbG9xdWVudC9CdWlsZGVyOylMZ2FhcmFzb24vZGF0YWJhc2UvY29udHJhY3QvZWxvcXVlbnQvQnVpbGRlcjt0AC9nYWFyYXNvbi9kYXRhYmFzZS90ZXN0L3BhcmVudC9TZXJpYWxpemFibGVUZXN0c3QAM2xhbWJkYSRSZWNvcmTluo_liJfljJZfc2VyaWFsaXplVG9TdHJpbmckNzAzODMyOWYkMXQAkShMZ2FhcmFzb24vZGF0YWJhc2UvdGVzdC9tb2RlbHMvcmVsYXRpb24vcG9qby9TdHVkZW50O0xnYWFyYXNvbi9kYXRhYmFzZS9jb250cmFjdC9lbG9xdWVudC9CdWlsZGVyOylMZ2FhcmFzb24vZGF0YWJhc2UvY29udHJhY3QvZWxvcXVlbnQvQnVpbGRlcjtxAH4ANXNxAH4AIAAAAAZ1cQB-ACQAAAAAdnIAMmdhYXJhc29uLmRhdGFiYXNlLmNvbnRyYWN0LmZ1bmN0aW9uLkJ1aWxkZXJXcmFwcGVyG4PKAOb6o0ECAAB4cHEAfgAzcQB-ADRxAH4ANXEAfgAzdAAYbGFtYmRhJHN0YXRpYyQ0OGIzYTdkOCQxcQB-ADVxAH4ANXNxAH4AIAAAAAZ1cQB-ACQAAAAAdnEAfgAAdAAxZ2FhcmFzb24vZGF0YWJhc2UvY29udHJhY3QvZnVuY3Rpb24vUmVjb3JkV3JhcHBlcnEAfgA0dABaKExnYWFyYXNvbi9kYXRhYmFzZS9jb250cmFjdC9lbG9xdWVudC9SZWNvcmQ7KUxnYWFyYXNvbi9kYXRhYmFzZS9jb250cmFjdC9lbG9xdWVudC9SZWNvcmQ7dAAlZ2FhcmFzb24vZGF0YWJhc2UvZWxvcXVlbnQvUmVjb3JkQmVhbnQAFmxhbWJkYSR3aXRoJDliY2JiOGE1JDFxAH4AQnEAfgBCcQB-ABp4eA==";

        Record<Student, Long> recordCopy = Record.deserialize(s);

        System.out.println(recordCopy);

        Student object = recordCopy.toObject();
        System.out.println(recordCopy);
        System.out.println(recordCopy.toString().length());
        System.out.println(object);
        System.out.println(object.toString().length());
        Assert.assertEquals(2, object.getId().intValue());
        Assert.assertNotNull(object.getTeachersBelongsToMany());


    }
//
//    // 抽象父类
//    public static abstract class Builder<B extends Builder<B, T, K>, T, K> {
//        // 泛型方法，返回调用它的类的实例类型
//        public abstract B getSelf();
//
//        // 其他父类方法...
//    }
//
//    // 子类
//    public static class MysqlBuilder<T, K> extends Builder<MysqlBuilder<T, K>, T, K > {
//        @Override
//        public MysqlBuilder<T, K> getSelf() {
//            return this;
//        }
//
//        // 子类特有的方法...
//    }
//
//    public void ttt() {
//        MysqlBuilder<Object, Object> objectObjectMysqlBuilder = new MysqlBuilder<>();
//
//
//    }

}
