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
        Builder<Student, Long> builder = studentModel.newQuery().where(Student::getId, 4);
        Builder<Student, Long> deepCopyBuilder = ObjectUtils.deepCopy(builder);
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

        Builder<Student, Long> builder = studentModel.newQuery().with("teachersBelongsToMany", b -> {
            return b.limit(student1.getAge());
        });

        String serialize = builder.serializeToString();
        System.out.println(serialize);
        Assert.assertFalse(ObjectUtils.isEmpty(serialize));
        System.out.println(serialize.length());


        List<Student> student = builder.get().toObjectList();
        Assert.assertFalse(ObjectUtils.isEmpty(student));
        Assert.assertNotNull(student.get(0).getTeachersBelongsToMany());

        Builder<Student, Long> builderCopy = Builder.deserialize(serialize);

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

    @Test
    public void Record反序列化() {
        String s = "rO0ABXNyACVnYWFyYXNvbi5kYXRhYmFzZS5lbG9xdWVudC5SZWNvcmRCZWFuAAAAAAAAAAEMAAB4cHdDAAZteXNxbDIAOWdhYXJhc29uLmRhdGFiYXNlLnRlc3QubW9kZWxzLnJlbGF0aW9uLm1vZGVsLlN0dWRlbnRNb2RlbHNyABFqYXZhLnV0aWwuSGFzaE1hcAUH2sHDFmDRAwACRgAKbG9hZEZhY3RvckkACXRocmVzaG9sZHhwP0AAAAAAAAx3CAAAABAAAAAIdAAKaXNfZGVsZXRlZHNyABFqYXZhLmxhbmcuQm9vbGVhbs0gcoDVnPruAgABWgAFdmFsdWV4cAB0AAp1cGRhdGVkX2F0c3IADmphdmEudXRpbC5EYXRlaGqBAUtZdBkDAAB4cHcIAAABKDAoPNh4dAAKdGVhY2hlcl9pZHNyAA5qYXZhLmxhbmcuTG9uZzuL5JDMjyPfAgABSgAFdmFsdWV4cgAQamF2YS5sYW5nLk51bWJlcoaslR0LlOCLAgAAeHAAAAAAAAAABnQAA3NleHNyABFqYXZhLmxhbmcuSW50ZWdlchLioKT3gYc4AgABSQAFdmFsdWV4cQB-AAwAAAACdAAEbmFtZXQABuWwj-W8oHQACmNyZWF0ZWRfYXRzcQB-AAh3CAAAASAD1cb4eHQAAmlkc3EAfgALAAAAAAAAAAJ0AANhZ2VzcQB-AA8AAAALeHd6AHhzZWxlY3QgYG5hbWVgLGBhZ2VgLGBzZXhgLGB0ZWFjaGVyX2lkYCxgaXNfZGVsZXRlZGAsYGNyZWF0ZWRfYXRgLGB1cGRhdGVkX2F0YCxgaWRgIGZyb20gc3R1ZGVudCB3aGVyZSBgaWRgPSA_ICBsaW1pdCAgPyBzcQB-AAI_QAAAAAAADHcIAAAAEAAAAAF0ABV0ZWFjaGVyc0JlbG9uZ3NUb01hbnlzcgAzZ2FhcmFzb24uZGF0YWJhc2UuY29udHJhY3QuZWxvcXVlbnQuUmVjb3JkJFJlbGF0aW9uAAAAAAAAAAECAANMABFyZWxhdGlvbkZpZWxkTmFtZXQAEkxqYXZhL2xhbmcvU3RyaW5nO0wAKXJlbGF0aW9uc2hpcFJlY29yZFdpdGhGdW5jdGlvbmFsSW50ZXJmYWNldABPTGdhYXJhc29uL2RhdGFiYXNlL2NvbnRyYWN0L2Z1bmN0aW9uL1JlbGF0aW9uc2hpcFJlY29yZFdpdGhGdW5jdGlvbmFsSW50ZXJmYWNlO0wAGnNxbFBhcnRGdW5jdGlvbmFsSW50ZXJmYWNldABITGdhYXJhc29uL2RhdGFiYXNlL2NvbnRyYWN0L2Z1bmN0aW9uL0dlbmVyYXRlU3FsUGFydEZ1bmN0aW9uYWxJbnRlcmZhY2U7eHBxAH4AGnNyACFqYXZhLmxhbmcuaW52b2tlLlNlcmlhbGl6ZWRMYW1iZGFvYdCULCk2hQIACkkADmltcGxNZXRob2RLaW5kWwAMY2FwdHVyZWRBcmdzdAATW0xqYXZhL2xhbmcvT2JqZWN0O0wADmNhcHR1cmluZ0NsYXNzdAARTGphdmEvbGFuZy9DbGFzcztMABhmdW5jdGlvbmFsSW50ZXJmYWNlQ2xhc3NxAH4AHEwAHWZ1bmN0aW9uYWxJbnRlcmZhY2VNZXRob2ROYW1lcQB-ABxMACJmdW5jdGlvbmFsSW50ZXJmYWNlTWV0aG9kU2lnbmF0dXJlcQB-ABxMAAlpbXBsQ2xhc3NxAH4AHEwADmltcGxNZXRob2ROYW1lcQB-ABxMABNpbXBsTWV0aG9kU2lnbmF0dXJlcQB-ABxMABZpbnN0YW50aWF0ZWRNZXRob2RUeXBlcQB-ABx4cAAAAAZ1cgATW0xqYXZhLmxhbmcuT2JqZWN0O5DOWJ8QcylsAgAAeHAAAAAAdnEAfgAAdABNZ2FhcmFzb24vZGF0YWJhc2UvY29udHJhY3QvZnVuY3Rpb24vUmVsYXRpb25zaGlwUmVjb3JkV2l0aEZ1bmN0aW9uYWxJbnRlcmZhY2V0AAdleGVjdXRldABaKExnYWFyYXNvbi9kYXRhYmFzZS9jb250cmFjdC9lbG9xdWVudC9SZWNvcmQ7KUxnYWFyYXNvbi9kYXRhYmFzZS9jb250cmFjdC9lbG9xdWVudC9SZWNvcmQ7dAAlZ2FhcmFzb24vZGF0YWJhc2UvZWxvcXVlbnQvUmVjb3JkQmVhbnQAFmxhbWJkYSR3aXRoJDg4YmZiMGVlJDFxAH4AKXEAfgApc3EAfgAgAAAABnVxAH4AJAAAAAFzcgAzZ2FhcmFzb24uZGF0YWJhc2UudGVzdC5tb2RlbHMucmVsYXRpb24ucG9qby5TdHVkZW50lNMw2KUPFh0CAAtMAANhZ2V0ABNMamF2YS9sYW5nL0ludGVnZXI7TAAJY3JlYXRlZEF0dAAQTGphdmEvdXRpbC9EYXRlO0wACWlzRGVsZXRlZHQAE0xqYXZhL2xhbmcvQm9vbGVhbjtMAARuYW1lcQB-ABxMABpyZWxhdGlvbnNoaXBTdHVkZW50VGVhY2hlcnQASExnYWFyYXNvbi9kYXRhYmFzZS90ZXN0L21vZGVscy9yZWxhdGlvbi9wb2pvL1JlbGF0aW9uc2hpcFN0dWRlbnRUZWFjaGVyO0wAG3JlbGF0aW9uc2hpcFN0dWRlbnRUZWFjaGVyc3QAEExqYXZhL3V0aWwvTGlzdDtMAANzZXhxAH4AL0wAB3RlYWNoZXJ0ADVMZ2FhcmFzb24vZGF0YWJhc2UvdGVzdC9tb2RlbHMvcmVsYXRpb24vcG9qby9UZWFjaGVyO0wACXRlYWNoZXJJZHQAEExqYXZhL2xhbmcvTG9uZztMABV0ZWFjaGVyc0JlbG9uZ3NUb01hbnlxAH4AM0wACXVwZGF0ZWRBdHEAfgAweHIAO2dhYXJhc29uLmRhdGFiYXNlLnRlc3QubW9kZWxzLnJlbGF0aW9uLnBvam8uYmFzZS5CYXNlRW50aXR5AAAAAAAAAAECAAFMAAJpZHEAfgA1eHBwc3EAfgAPAAAADHBwcHBwcHBwcHB2cgAvZ2FhcmFzb24uZGF0YWJhc2UudGVzdC5wYXJlbnQuU2VyaWFsaXphYmxlVGVzdHMAAAAAAAAAAAAAAHhwdABGZ2FhcmFzb24vZGF0YWJhc2UvY29udHJhY3QvZnVuY3Rpb24vR2VuZXJhdGVTcWxQYXJ0RnVuY3Rpb25hbEludGVyZmFjZXEAfgAodABcKExnYWFyYXNvbi9kYXRhYmFzZS9jb250cmFjdC9lbG9xdWVudC9CdWlsZGVyOylMZ2FhcmFzb24vZGF0YWJhc2UvY29udHJhY3QvZWxvcXVlbnQvQnVpbGRlcjt0AC9nYWFyYXNvbi9kYXRhYmFzZS90ZXN0L3BhcmVudC9TZXJpYWxpemFibGVUZXN0c3QAM2xhbWJkYSRSZWNvcmTluo_liJfljJZfc2VyaWFsaXplVG9TdHJpbmckZTFkMDJiYWQkMXQAkShMZ2FhcmFzb24vZGF0YWJhc2UvdGVzdC9tb2RlbHMvcmVsYXRpb24vcG9qby9TdHVkZW50O0xnYWFyYXNvbi9kYXRhYmFzZS9jb250cmFjdC9lbG9xdWVudC9CdWlsZGVyOylMZ2FhcmFzb24vZGF0YWJhc2UvY29udHJhY3QvZWxvcXVlbnQvQnVpbGRlcjtxAH4APHh4";

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
}
