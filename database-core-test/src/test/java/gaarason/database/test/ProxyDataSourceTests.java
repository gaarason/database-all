package gaarason.database.test;

import gaarason.database.connections.ProxyDataSource;
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
public class ProxyDataSourceTests extends BaseTests {

    /*********  以下分别使用不同的数据库连接, 理解成物理上不同的数据库即可  ***********/
    private static StudentModel studentModel = new StudentModel();

    private static Student2Model student2Model = new Student2Model();

    private static Student3Model student3Model = new Student3Model();

    protected List<DataSource> getDataSourceList() {
        ProxyDataSource proxyDataSource = studentModel.getDataSource();
        return proxyDataSource.getMasterDataSourceList();
    }


    @Test
    public void 事物状态在各个proxyDataSource中相互独立() throws InterruptedException {
        ProxyDataSource proxyDataSource  = studentModel.getDataSource();
        ProxyDataSource proxyDataSource1 = student2Model.getDataSource();

        proxyDataSource.setInTransaction();
        boolean q = proxyDataSource.isInTransaction();
        boolean w = proxyDataSource1.isInTransaction();
        Assert.assertTrue(q);
        Assert.assertFalse(w);
        proxyDataSource.setOutTransaction();
        boolean e = proxyDataSource.isInTransaction();
        Assert.assertFalse(e);
    }

    @Test
    public void 同一个proxyDataSource的事物状态在各个线程中相互独立() throws InterruptedException {
        ProxyDataSource proxyDataSource = studentModel.getDataSource();

        MultiThreadUtil.run(100, 10, () -> {
            boolean be = proxyDataSource.isInTransaction();
            proxyDataSource.setInTransaction();
            boolean in = proxyDataSource.isInTransaction();
            proxyDataSource.setOutTransaction();
            boolean af = proxyDataSource.isInTransaction();
            Assert.assertFalse(be);
            Assert.assertTrue(in);
            Assert.assertFalse(af);
        });
    }

    @Test
    public void 多个proxyDataSource的事物状态在各个线程中相互独立() throws InterruptedException {
        ProxyDataSource proxyDataSource  = studentModel.getDataSource();
        ProxyDataSource proxyDataSource1 = student2Model.getDataSource();
        ProxyDataSource proxyDataSource2 = student3Model.getDataSource();

        MultiThreadUtil.run(100, 10, () -> {
            boolean be = proxyDataSource.isInTransaction();
            proxyDataSource.setInTransaction();
            boolean in = proxyDataSource.isInTransaction();
            proxyDataSource.setOutTransaction();
            boolean af = proxyDataSource.isInTransaction();
            Assert.assertFalse(be);
            Assert.assertTrue(in);
            Assert.assertFalse(af);
            boolean be1 = proxyDataSource1.isInTransaction();
            proxyDataSource1.setInTransaction();
            boolean in1 = proxyDataSource1.isInTransaction();
            proxyDataSource1.setOutTransaction();
            boolean af1 = proxyDataSource1.isInTransaction();
            Assert.assertFalse(be1);
            Assert.assertTrue(in1);
            Assert.assertFalse(af1);
            boolean be2 = proxyDataSource2.isInTransaction();
            proxyDataSource2.setInTransaction();
            boolean in2 = proxyDataSource2.isInTransaction();
            proxyDataSource2.setOutTransaction();
            boolean af2 = proxyDataSource2.isInTransaction();
            Assert.assertFalse(be2);
            Assert.assertTrue(in2);
            Assert.assertFalse(af2);
        });
    }

}
