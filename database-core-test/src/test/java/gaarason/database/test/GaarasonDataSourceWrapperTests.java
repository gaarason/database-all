package gaarason.database.test;

import gaarason.database.connection.GaarasonDataSourceWrapper;
import gaarason.database.test.models.Student2Model;
import gaarason.database.test.models.Student3Model;
import gaarason.database.test.models.StudentModel;
import gaarason.database.test.parent.BaseTests;
import gaarason.database.test.utils.MultiThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.sql.DataSource;
import java.util.List;

/**
 * `传播性`为同数据库连接不可嵌套, 不同的数据库连接可以任意嵌套
 */
@Slf4j
@FixMethodOrder(MethodSorters.JVM)
public class GaarasonDataSourceWrapperTests extends BaseTests {

    /*********  以下分别使用不同的数据库连接, 理解成物理上不同的数据库即可  ***********/
    private static StudentModel studentModel = new StudentModel();

    private static Student2Model student2Model = new Student2Model();

    private static Student3Model student3Model = new Student3Model();

    protected List<DataSource> getDataSourceList() {
        GaarasonDataSourceWrapper gaarasonDataSourceWrapper = studentModel.getGaarasonDataSource();
        return gaarasonDataSourceWrapper.getMasterDataSourceList();
    }


//    @Test
//    public void 事物状态在各个proxyDataSource中相互独立() throws InterruptedException {
//        GaarasonDataSourceWrapper gaarasonDataSourceWrapper  = studentModel.getGaarasonDataSource();
//        GaarasonDataSourceWrapper gaarasonDataSourceWrapper1 = student2Model.getGaarasonDataSource();
//
//        gaarasonDataSourceWrapper.setInTransaction();
//        boolean q = gaarasonDataSourceWrapper.isInTransaction();
//        boolean w = gaarasonDataSourceWrapper1.isInTransaction();
//        Assert.assertTrue(q);
//        Assert.assertFalse(w);
//        gaarasonDataSourceWrapper.setOutTransaction();
//        boolean e = gaarasonDataSourceWrapper.isInTransaction();
//        Assert.assertFalse(e);
//    }
//
//    @Test
//    public void 同一个proxyDataSource的事物状态在各个线程中相互独立() throws InterruptedException {
//        GaarasonDataSourceWrapper gaarasonDataSourceWrapper = studentModel.getGaarasonDataSource();
//
//        MultiThreadUtil.run(100, 10, () -> {
//            boolean be = gaarasonDataSourceWrapper.isInTransaction();
//            gaarasonDataSourceWrapper.setInTransaction();
//            boolean in = gaarasonDataSourceWrapper.isInTransaction();
//            gaarasonDataSourceWrapper.setOutTransaction();
//            boolean af = gaarasonDataSourceWrapper.isInTransaction();
//            Assert.assertFalse(be);
//            Assert.assertTrue(in);
//            Assert.assertFalse(af);
//        });
//    }
//
//    @Test
//    public void 多个proxyDataSource的事物状态在各个线程中相互独立() throws InterruptedException {
//        GaarasonDataSourceWrapper gaarasonDataSourceWrapper  = studentModel.getGaarasonDataSource();
//        GaarasonDataSourceWrapper gaarasonDataSourceWrapper1 = student2Model.getGaarasonDataSource();
//        GaarasonDataSourceWrapper gaarasonDataSourceWrapper2 = student3Model.getGaarasonDataSource();
//
//        MultiThreadUtil.run(100, 10, () -> {
//            boolean be = gaarasonDataSourceWrapper.isInTransaction();
//            gaarasonDataSourceWrapper.setInTransaction();
//            boolean in = gaarasonDataSourceWrapper.isInTransaction();
//            gaarasonDataSourceWrapper.setOutTransaction();
//            boolean af = gaarasonDataSourceWrapper.isInTransaction();
//            Assert.assertFalse(be);
//            Assert.assertTrue(in);
//            Assert.assertFalse(af);
//            boolean be1 = gaarasonDataSourceWrapper1.isInTransaction();
//            gaarasonDataSourceWrapper1.setInTransaction();
//            boolean in1 = gaarasonDataSourceWrapper1.isInTransaction();
//            gaarasonDataSourceWrapper1.setOutTransaction();
//            boolean af1 = gaarasonDataSourceWrapper1.isInTransaction();
//            Assert.assertFalse(be1);
//            Assert.assertTrue(in1);
//            Assert.assertFalse(af1);
//            boolean be2 = gaarasonDataSourceWrapper2.isInTransaction();
//            gaarasonDataSourceWrapper2.setInTransaction();
//            boolean in2 = gaarasonDataSourceWrapper2.isInTransaction();
//            gaarasonDataSourceWrapper2.setOutTransaction();
//            boolean af2 = gaarasonDataSourceWrapper2.isInTransaction();
//            Assert.assertFalse(be2);
//            Assert.assertTrue(in2);
//            Assert.assertFalse(af2);
//        });
//    }

}
