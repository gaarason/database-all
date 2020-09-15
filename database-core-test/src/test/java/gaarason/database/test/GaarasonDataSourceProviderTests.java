package gaarason.database.test;

import gaarason.database.connection.GaarasonDataSourceProvider;
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
public class GaarasonDataSourceProviderTests extends BaseTests {

    /*********  以下分别使用不同的数据库连接, 理解成物理上不同的数据库即可  ***********/
    private static StudentModel studentModel = new StudentModel();

    private static Student2Model student2Model = new Student2Model();

    private static Student3Model student3Model = new Student3Model();

    protected List<DataSource> getDataSourceList() {
        GaarasonDataSourceProvider gaarasonDataSourceProvider = studentModel.getGaarasonDataSource();
        return gaarasonDataSourceProvider.getMasterDataSourceList();
    }


    @Test
    public void 事物状态在各个proxyDataSource中相互独立() throws InterruptedException {
        GaarasonDataSourceProvider gaarasonDataSourceProvider = studentModel.getGaarasonDataSource();
        GaarasonDataSourceProvider gaarasonDataSourceProvider1 = student2Model.getGaarasonDataSource();

        gaarasonDataSourceProvider.setInTransaction();
        boolean q = gaarasonDataSourceProvider.isInTransaction();
        boolean w = gaarasonDataSourceProvider1.isInTransaction();
        Assert.assertTrue(q);
        Assert.assertFalse(w);
        gaarasonDataSourceProvider.setOutTransaction();
        boolean e = gaarasonDataSourceProvider.isInTransaction();
        Assert.assertFalse(e);
    }

    @Test
    public void 同一个proxyDataSource的事物状态在各个线程中相互独立() throws InterruptedException {
        GaarasonDataSourceProvider gaarasonDataSourceProvider = studentModel.getGaarasonDataSource();

        MultiThreadUtil.run(100, 10, () -> {
            boolean be = gaarasonDataSourceProvider.isInTransaction();
            gaarasonDataSourceProvider.setInTransaction();
            boolean in = gaarasonDataSourceProvider.isInTransaction();
            gaarasonDataSourceProvider.setOutTransaction();
            boolean af = gaarasonDataSourceProvider.isInTransaction();
            Assert.assertFalse(be);
            Assert.assertTrue(in);
            Assert.assertFalse(af);
        });
    }

    @Test
    public void 多个proxyDataSource的事物状态在各个线程中相互独立() throws InterruptedException {
        GaarasonDataSourceProvider gaarasonDataSourceProvider = studentModel.getGaarasonDataSource();
        GaarasonDataSourceProvider gaarasonDataSourceProvider1 = student2Model.getGaarasonDataSource();
        GaarasonDataSourceProvider gaarasonDataSourceProvider2 = student3Model.getGaarasonDataSource();

        MultiThreadUtil.run(100, 10, () -> {
            boolean be = gaarasonDataSourceProvider.isInTransaction();
            gaarasonDataSourceProvider.setInTransaction();
            boolean in = gaarasonDataSourceProvider.isInTransaction();
            gaarasonDataSourceProvider.setOutTransaction();
            boolean af = gaarasonDataSourceProvider.isInTransaction();
            Assert.assertFalse(be);
            Assert.assertTrue(in);
            Assert.assertFalse(af);
            boolean be1 = gaarasonDataSourceProvider1.isInTransaction();
            gaarasonDataSourceProvider1.setInTransaction();
            boolean in1 = gaarasonDataSourceProvider1.isInTransaction();
            gaarasonDataSourceProvider1.setOutTransaction();
            boolean af1 = gaarasonDataSourceProvider1.isInTransaction();
            Assert.assertFalse(be1);
            Assert.assertTrue(in1);
            Assert.assertFalse(af1);
            boolean be2 = gaarasonDataSourceProvider2.isInTransaction();
            gaarasonDataSourceProvider2.setInTransaction();
            boolean in2 = gaarasonDataSourceProvider2.isInTransaction();
            gaarasonDataSourceProvider2.setOutTransaction();
            boolean af2 = gaarasonDataSourceProvider2.isInTransaction();
            Assert.assertFalse(be2);
            Assert.assertTrue(in2);
            Assert.assertFalse(af2);
        });
    }

}
