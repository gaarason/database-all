package gaarason.database.test;

import gaarason.database.connections.ProxyDataSource;
import gaarason.database.eloquent.Record;
import gaarason.database.test.parent.BaseTests;
import gaarason.database.test.relation.data.model.StudentBelongsToManyModel;
import gaarason.database.test.relation.data.model.StudentHasOneModel;
import gaarason.database.test.relation.data.model.StudentModel;
import gaarason.database.test.relation.data.model.TeacherHasManyModel;
import gaarason.database.test.relation.data.pojo.StudentBelongsToMany;
import gaarason.database.test.relation.data.pojo.StudentHasOne;
import gaarason.database.test.relation.data.pojo.TeacherHasMany;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.MethodSorters;

import javax.sql.DataSource;
import java.util.List;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
public class RelationTests extends BaseTests {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static StudentModel studentModel = new StudentModel();

    private static StudentHasOneModel studentHasOneModel = new StudentHasOneModel();

    private static TeacherHasManyModel teacherHasManyModel = new TeacherHasManyModel();

    private static StudentBelongsToManyModel studentBelongsToManyModel = new StudentBelongsToManyModel();

    protected List<DataSource> getDataSourceList() {
        ProxyDataSource proxyDataSource = studentModel.getProxyDataSource();
        return proxyDataSource.getMasterDataSourceList();
    }

    @Test
    public void 一对一关系() {
        StudentHasOne studentHasOne = studentHasOneModel.newQuery().firstOrFail().toObject();
        System.out.println(studentHasOne);
        System.out.println(studentHasOne.getTeacher());
        Assert.assertNotNull(studentHasOne.getTeacher());
        Assert.assertEquals((long) studentHasOne.getTeacher().getId(), 6);
    }

    @Test
    public void 一对多关系() {
        Record<TeacherHasMany> record         = teacherHasManyModel.newQuery().where("id", "6").firstOrFail();
        TeacherHasMany         teacherHasMany = record.toObject();
        System.out.println(teacherHasMany);
        Assert.assertEquals(teacherHasMany.getStudents().size(), 4);
    }

    @Test
    public void 多对多关系() {
        Record<StudentBelongsToMany> record               = studentBelongsToManyModel.newQuery()
            .where("id", "6")
            .firstOrFail();
        StudentBelongsToMany         studentBelongsToMany = record.toObject();
        System.out.println(studentBelongsToMany);
        Assert.assertEquals(studentBelongsToMany.getTeachers().size(), 2);

    }
}
