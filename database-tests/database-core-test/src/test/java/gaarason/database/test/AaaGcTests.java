package gaarason.database.test;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.test.models.normal.StudentModel;
import gaarason.database.test.parent.base.BaseTests;
import gaarason.database.test.utils.MultiThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
public class AaaGcTests extends BaseTests {

    private static final StudentModel studentModel = new StudentModel();

    @Override
    protected GaarasonDataSource getGaarasonDataSource() {
        return studentModel.getGaarasonDataSource();
    }

    @Test
    public void testEntityUtil() {

    }

    @Test
    public void testPressure() throws InterruptedException {
        while (true) {
            MultiThreadUtil.run(10,100, () ->{
                StudentModel.Entity entity = studentModel.newQuery()
                    .select(StudentModel.Entity::getId)
                    .select(StudentModel.Entity::getName)
                    .select(StudentModel.Entity::getAge)
                    .where(StudentModel.Entity::getCreatedAt, ">=", "2009-03-15 22:15:23")
                    .firstOrFail().toObject();
                Assert.assertEquals(entity.getId().intValue(), 9);
            });
        }

    }

    @Test
    public void testLow() throws InterruptedException {
        while (true) {

        }

    }

}
