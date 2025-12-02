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
    public void newTest() {
        studentModel.newQuery().with(Student::getTeacher, builder -> builder).get().toObjectList();
    }

    @Test
    public void RecordList序列化_serializeToString() {
        Student student1 = new Student();
        student1.setAge(3);

        RecordList<Student, Long> records = studentModel.newQuery()
                .with("teachersBelongsToMany", builder -> builder.limit(1)).get();

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
        String s = "rO0ABXNyACVnYWFyYXNvbi5kYXRhYmFzZS5lbG9xdWVudC5SZWNvcmRCZWFuAAAAAAAAAAEMAAB4cHdDAAZteXNxbDIAOWdhYXJhc29uLmRhdGFiYXNlLnRlc3QubW9kZWxzLnJlbGF0aW9uLm1vZGVsLlN0dWRlbnRNb2RlbHNyABFqYXZhLnV0aWwuSGFzaE1hcAUH2sHDFmDRAwACRgAKbG9hZEZhY3RvckkACXRocmVzaG9sZHhwP0AAAAAAAAx3CAAAABAAAAAIdAAKaXNfZGVsZXRlZHNyABFqYXZhLmxhbmcuQm9vbGVhbs0gcoDVnPruAgABWgAFdmFsdWV4cAB0AAp1cGRhdGVkX2F0c3IADmphdmEudXRpbC5EYXRlaGqBAUtZdBkDAAB4cHcIAAABKDAoPNh4dAAKdGVhY2hlcl9pZHNyAA5qYXZhLmxhbmcuTG9uZzuL5JDMjyPfAgABSgAFdmFsdWV4cgAQamF2YS5sYW5nLk51bWJlcoaslR0LlOCLAgAAeHAAAAAAAAAABnQAA3NleHNyABFqYXZhLmxhbmcuSW50ZWdlchLioKT3gYc4AgABSQAFdmFsdWV4cQB-AAwAAAACdAAEbmFtZXQABuWwj-W8oHQACmNyZWF0ZWRfYXRzcQB-AAh3CAAAASAD1cb4eHQAAmlkc3EAfgALAAAAAAAAAAJ0AANhZ2VzcQB-AA8AAAALeHNxAH4AAj9AAAAAAAAMdwgAAAAQAAAACHEAfgAEcQB-AAZxAH4AB3EAfgAJcQB-AApxAH4ADXEAfgAOcQB-ABBxAH4AEXEAfgAScQB-ABNxAH4AFHEAfgAVcQB-ABZxAH4AF3EAfgAYeHoAAAIzAjFzZWxlY3QgYHN0dWRlbnRfNDM1MzYyOTQ5YC5gbmFtZWAsYHN0dWRlbnRfNDM1MzYyOTQ5YC5gYWdlYCxgc3R1ZGVudF80MzUzNjI5NDlgLmBzZXhgLGBzdHVkZW50XzQzNTM2Mjk0OWAuYHRlYWNoZXJfaWRgLGBzdHVkZW50XzQzNTM2Mjk0OWAuYGlzX2RlbGV0ZWRgLGBzdHVkZW50XzQzNTM2Mjk0OWAuYGNyZWF0ZWRfYXRgLGBzdHVkZW50XzQzNTM2Mjk0OWAuYHVwZGF0ZWRfYXRgLGBzdHVkZW50XzQzNTM2Mjk0OWAuYGlkYCBmcm9tIGBzdHVkZW50YCBhcyBgc3R1ZGVudF80MzUzNjI5NDlgIHdoZXJlIChgc3R1ZGVudF80MzUzNjI5NDlgLmBpc19kZWxldGVkYD0gPyAgYW5kIGBzdHVkZW50XzQzNTM2Mjk0OWAuYGlkYGluKHNlbGVjdCBgdGVhY2hlcl8xNDI3ODkyMTQ1YC5gaWRgIGZyb20gYHRlYWNoZXJgIGFzIGB0ZWFjaGVyXzE0Mjc4OTIxNDVgKSBvciAoYHN0dWRlbnRfNDM1MzYyOTQ5YC5gaXNfZGVsZXRlZGA9ID8gIGFuZCAxKSkgYW5kIGBzdHVkZW50XzQzNTM2Mjk0OWAuYGlzX2RlbGV0ZWRgPSA_ICBhbmQgYHN0dWRlbnRfNDM1MzYyOTQ5YC5gaWRgPSA_ICBsaW1pdCAgPyBzcQB-AAI_QAAAAAAADHcIAAAAEAAAAAF0ABV0ZWFjaGVyc0JlbG9uZ3NUb01hbnlzcgAzZ2FhcmFzb24uZGF0YWJhc2UuY29udHJhY3QuZWxvcXVlbnQuUmVjb3JkJFJlbGF0aW9uAAAAAAAAAAECAAVaABFyZWxhdGlvbk9wZXJhdGlvbkwADWN1c3RvbUJ1aWxkZXJ0ADdMZ2FhcmFzb24vZGF0YWJhc2UvY29udHJhY3QvZnVuY3Rpb24vQnVpbGRlckFueVdyYXBwZXI7TAAQb3BlcmF0aW9uQnVpbGRlcnEAfgAdTAANcmVjb3JkV3JhcHBlcnQAM0xnYWFyYXNvbi9kYXRhYmFzZS9jb250cmFjdC9mdW5jdGlvbi9SZWNvcmRXcmFwcGVyO0wAEXJlbGF0aW9uRmllbGROYW1ldAASTGphdmEvbGFuZy9TdHJpbmc7eHAAc3IAIWphdmEubGFuZy5pbnZva2UuU2VyaWFsaXplZExhbWJkYW9h0JQsKTaFAgAKSQAOaW1wbE1ldGhvZEtpbmRbAAxjYXB0dXJlZEFyZ3N0ABNbTGphdmEvbGFuZy9PYmplY3Q7TAAOY2FwdHVyaW5nQ2xhc3N0ABFMamF2YS9sYW5nL0NsYXNzO0wAGGZ1bmN0aW9uYWxJbnRlcmZhY2VDbGFzc3EAfgAfTAAdZnVuY3Rpb25hbEludGVyZmFjZU1ldGhvZE5hbWVxAH4AH0wAImZ1bmN0aW9uYWxJbnRlcmZhY2VNZXRob2RTaWduYXR1cmVxAH4AH0wACWltcGxDbGFzc3EAfgAfTAAOaW1wbE1ldGhvZE5hbWVxAH4AH0wAE2ltcGxNZXRob2RTaWduYXR1cmVxAH4AH0wAFmluc3RhbnRpYXRlZE1ldGhvZFR5cGVxAH4AH3hwAAAABnVyABNbTGphdmEubGFuZy5PYmplY3Q7kM5YnxBzKWwCAAB4cAAAAAFzcgAzZ2FhcmFzb24uZGF0YWJhc2UudGVzdC5tb2RlbHMucmVsYXRpb24ucG9qby5TdHVkZW50uVfUNKgoNHsCAAxMAANhZ2V0ABNMamF2YS9sYW5nL0ludGVnZXI7TAAJY3JlYXRlZEF0dAAQTGphdmEvdXRpbC9EYXRlO0wACWlzRGVsZXRlZHQAE0xqYXZhL2xhbmcvQm9vbGVhbjtMAARuYW1lcQB-AB9MABpyZWxhdGlvbnNoaXBTdHVkZW50VGVhY2hlcnQASExnYWFyYXNvbi9kYXRhYmFzZS90ZXN0L21vZGVscy9yZWxhdGlvbi9wb2pvL1JlbGF0aW9uc2hpcFN0dWRlbnRUZWFjaGVyO0wAG3JlbGF0aW9uc2hpcFN0dWRlbnRUZWFjaGVyc3QAEExqYXZhL3V0aWwvTGlzdDtMAARzZWxmdAA1TGdhYXJhc29uL2RhdGFiYXNlL3Rlc3QvbW9kZWxzL3JlbGF0aW9uL3Bvam8vU3R1ZGVudDtMAANzZXhxAH4AKEwAB3RlYWNoZXJ0ADVMZ2FhcmFzb24vZGF0YWJhc2UvdGVzdC9tb2RlbHMvcmVsYXRpb24vcG9qby9UZWFjaGVyO0wACXRlYWNoZXJJZHQAEExqYXZhL2xhbmcvTG9uZztMABV0ZWFjaGVyc0JlbG9uZ3NUb01hbnlxAH4ALEwACXVwZGF0ZWRBdHEAfgApeHIAO2dhYXJhc29uLmRhdGFiYXNlLnRlc3QubW9kZWxzLnJlbGF0aW9uLnBvam8uYmFzZS5CYXNlRW50aXR5AAAAAAAAAAECAAFMAAJpZHEAfgAveHBwc3EAfgAPAAAADHBwcHBwcHBwcHBwdnIAL2dhYXJhc29uLmRhdGFiYXNlLnRlc3QucGFyZW50LlNlcmlhbGl6YWJsZVRlc3RzAAAAAAAAAAAAAAB4cHQANWdhYXJhc29uL2RhdGFiYXNlL2NvbnRyYWN0L2Z1bmN0aW9uL0J1aWxkZXJBbnlXcmFwcGVydAAHZXhlY3V0ZXQAXChMZ2FhcmFzb24vZGF0YWJhc2UvY29udHJhY3QvZWxvcXVlbnQvQnVpbGRlcjspTGdhYXJhc29uL2RhdGFiYXNlL2NvbnRyYWN0L2Vsb3F1ZW50L0J1aWxkZXI7dAAvZ2FhcmFzb24vZGF0YWJhc2UvdGVzdC9wYXJlbnQvU2VyaWFsaXphYmxlVGVzdHN0ADNsYW1iZGEkUmVjb3Jk5bqP5YiX5YyWX3NlcmlhbGl6ZVRvU3RyaW5nJDk5MjE4N2Q1JDF0AJEoTGdhYXJhc29uL2RhdGFiYXNlL3Rlc3QvbW9kZWxzL3JlbGF0aW9uL3Bvam8vU3R1ZGVudDtMZ2FhcmFzb24vZGF0YWJhc2UvY29udHJhY3QvZWxvcXVlbnQvQnVpbGRlcjspTGdhYXJhc29uL2RhdGFiYXNlL2NvbnRyYWN0L2Vsb3F1ZW50L0J1aWxkZXI7cQB-ADdzcQB-ACEAAAAGdXEAfgAlAAAAAHZyADVnYWFyYXNvbi5kYXRhYmFzZS5jb250cmFjdC5mdW5jdGlvbi5CdWlsZGVyQW55V3JhcHBlciLymlh9w5ugAgAAeHBxAH4ANXEAfgA2cQB-ADdxAH4ANXQAGGxhbWJkYSRzdGF0aWMkYmRlMWFkMGUkMXEAfgA3cQB-ADdzcQB-ACEAAAAGdXEAfgAlAAAAAHZxAH4AAHQAMWdhYXJhc29uL2RhdGFiYXNlL2NvbnRyYWN0L2Z1bmN0aW9uL1JlY29yZFdyYXBwZXJxAH4ANnQAWihMZ2FhcmFzb24vZGF0YWJhc2UvY29udHJhY3QvZWxvcXVlbnQvUmVjb3JkOylMZ2FhcmFzb24vZGF0YWJhc2UvY29udHJhY3QvZWxvcXVlbnQvUmVjb3JkO3QAJWdhYXJhc29uL2RhdGFiYXNlL2Vsb3F1ZW50L1JlY29yZEJlYW50ABZsYW1iZGEkd2l0aCQ3YTdkNWZhNyQxcQB-AERxAH4ARHEAfgAbeHg=";
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
//    public static class MySqlBuilderV2<T, K> extends Builder<MySqlBuilderV2<T, K>, T, K > {
//        @Override
//        public MySqlBuilderV2<T, K> getSelf() {
//            return this;
//        }
//
//        // 子类特有的方法...
//    }
//
//    public void ttt() {
//        MySqlBuilderV2<Object, Object> objectObjectMySqlBuilderV2 = new MySqlBuilderV2<>();
//
//
//    }

}
