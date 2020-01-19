package gaarason.database.test;

import gaarason.database.connections.ProxyDataSource;
import gaarason.database.exception.NestedTransactionException;
import gaarason.database.test.models.Student2Model;
import gaarason.database.test.models.Student3Model;
import gaarason.database.test.models.StudentModel;
import gaarason.database.test.parent.BaseTests;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.MethodSorters;

import javax.sql.DataSource;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
public class TransactionTests extends BaseTests {

    private static StudentModel studentModel = new StudentModel();

    private static Student2Model student2Model = new Student2Model();

    private static Student3Model student3Model = new Student3Model();

    protected List<DataSource> getDataSourceList() {
        ProxyDataSource proxyDataSource = studentModel.getProxyDataSource();
        return proxyDataSource.getMasterDataSourceList();
    }


    @Test(expected = NestedTransactionException.class)
    public void 事物_单个数据连接不可嵌套事物() {
        // 1层事物
        studentModel.newQuery().transaction(() -> {
            // 2层事物
            studentModel.newQuery().transaction(() -> {
                // 3层事物
                studentModel.newQuery().transaction(() -> {
                    try {
                        // 4层事物
                        studentModel.newQuery().transaction(() -> {
                            studentModel.newQuery().where("id", "1").data("name", "dddddd").update();
                            StudentModel.Entity entity = studentModel.newQuery()
                                .where("id", "1")
                                .firstOrFail()
                                .toObject();
                            Assert.assertEquals(entity.getName(), "dddddd");
                            throw new RuntimeException("业务上抛了个异常");
                        }, 1);
                    } catch (RuntimeException e) {
                    }
                }, 1);
            }, 1);
        }, 3);
    }

    @Test
    public void 事物_lock_in_share_mode() {
        studentModel.newQuery().transaction(() -> {
            studentModel.newQuery().where("id", "3").sharedLock().get();
        }, 3);
    }

    @Test
    public void 事物_for_update() {
        studentModel.newQuery().transaction(() -> {
            studentModel.newQuery().where("id", "3").lockForUpdate().get();
        }, 3);
    }


    @Test
    public void 事物_多线程下_多个数据连接嵌套事物2() throws InterruptedException {
        int            count          = 100;
        CountDownLatch countDownLatch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            new Thread(() -> {
                try {
                    // 1层事物
                    studentModel.newQuery().transaction(() -> {
                        studentModel.newQuery().data("name", "testttt").where("id", "9").update();
                        // 2层事物
                        student2Model.newQuery().transaction(() -> {
                            student2Model.newQuery().data("name", "testttt").where("id", "4").update();
                            try {
                                // 3层事物
                                student3Model.newQuery().transaction(() -> {
                                    student3Model.newQuery().where("id", "1").data("name", "dddddd").update();
                                    Student3Model.Entity entity = student3Model.newQuery()
                                        .where("id", "1")
                                        .firstOrFail()
                                        .toObject();
                                    throw new RuntimeException("业务上抛了个异常");
                                }, 1);
                            } catch (RuntimeException e) {
                                log.info("student3Model 业务上抛了个异常, 成功捕获, 所以student3Model上的事物回滚");
                            }
                            // student3Model 回滚
                            Student3Model.Entity entity = student3Model.newQuery()
                                .where("id", "1")
                                .firstOrFail()
                                .toObject();
                            // student2Model 不受影响
                            Student2Model.Entity id = student2Model.newQuery()
                                .where("id", "4")
                                .firstOrFail().toObject();
                        }, 1);
                        StudentModel.Entity id = studentModel.newQuery()
                            .where("id", "9")
                            .firstOrFail().toObject();
                    }, 3);
                } finally {
                    countDownLatch.countDown();
                    System.out.println("子线程结束");
                }
            }).start();
            System.out.println("开启线程: " + i);
        }
        countDownLatch.await();
        System.out.println("所有线程结束");
    }

    @Test
    public void 事物_多个数据连接嵌套事物() {
        // 1层事物
        studentModel.newQuery().transaction(() -> {
            System.out.println("studentModel 1层事物中");
            studentModel.newQuery().data("name", "testttt").where("id", "9").update();
            // 2层事物
            student2Model.newQuery().transaction(() -> {
                System.out.println("student2Model 1层事物中 studentModel 2层事物中");
                student2Model.newQuery().data("name", "testttt").where("id", "4").update();

                // 3层事物
                student3Model.newQuery().transaction(() -> {
                    Student3Model.Entity entity = student3Model.newQuery()
                        .where("id", "1")
                        .firstOrFail()
                        .toObject();
                }, 1);

                try {
                    // 3层事物
                    student3Model.newQuery().transaction(() -> {
                        System.out.println("student3Model 1层事物中 student2Model 2层事物中 studentModel 3层事物中");
                        student3Model.newQuery().where("id", "1").data("name", "ddddddxx").update();
                        Student3Model.Entity entity = student3Model.newQuery()
                            .where("id", "1")
                            .firstOrFail()
                            .toObject();
                        Assert.assertEquals(entity.getName(), "ddddddxx");
                        throw new RuntimeException("业务上抛了个异常");
                    }, 1);
                } catch (RuntimeException e) {
                    log.info("student3Model 业务上抛了个异常, 成功捕获, 所以student3Model上的事物回滚");
                }
                // student3Model 回滚
                Student3Model.Entity entity = student3Model.newQuery().where("id", "1").firstOrFail().toObject();
                Assert.assertNotEquals(entity.getName(), "ddddddxx");

                // student2Model 不受影响
                Student2Model.Entity id = student2Model.newQuery()
                    .where("id", "4")
                    .firstOrFail().toObject();
                Assert.assertEquals(id.getName(), "testttt");

            }, 1);
            StudentModel.Entity id = studentModel.newQuery()
                .where("id", "9")
                .firstOrFail().toObject();
            Assert.assertEquals(id.getName(), "testttt");
        }, 3);

        // 事物结束后
        StudentModel.Entity id = studentModel.newQuery()
            .where("id", "9")
            .firstOrFail().toObject();
        Assert.assertEquals(id.getName(), "testttt");

        Student2Model.Entity id2 = student2Model.newQuery()
            .where("id", "4")
            .firstOrFail().toObject();
        Assert.assertEquals(id2.getName(), "testttt");

        Student3Model.Entity entity = student3Model.newQuery().where("id", "1").firstOrFail().toObject();
        Assert.assertNotEquals(entity.getName(), "ddddddxx");

    }

    @Test
    public void 事物_多线程下_多个数据连接嵌套事物() throws InterruptedException {
        int            count          = 100;
        CountDownLatch countDownLatch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            new Thread(() -> {
                // 1层事物
                studentModel.newQuery().transaction(() -> {
                    studentModel.newQuery().data("name", "testttt").where("id", "9").update();
                    // 2层事物
                    student2Model.newQuery().transaction(() -> {
                        student2Model.newQuery().data("name", "testttt").where("id", "4").update();
                        try {
                            // 3层事物
                            student3Model.newQuery().transaction(() -> {
                                student3Model.newQuery().where("id", "1").data("name", "dddddd").update();
                                Student3Model.Entity entity = student3Model.newQuery()
                                    .where("id", "1")
                                    .firstOrFail()
                                    .toObject();
//                                Assert.assertEquals(entity.getName(), "dddddd");
                                throw new RuntimeException("业务上抛了个异常");
                            }, 1);
                        } catch (RuntimeException e) {
                            log.info("student3Model 业务上抛了个异常, 成功捕获, 所以student3Model上的事物回滚");
                        }
                        // student3Model 回滚
                        Student3Model.Entity entity = student3Model.newQuery()
                            .where("id", "1")
                            .firstOrFail()
                            .toObject();
//                        Assert.assertNotEquals(entity.getName(), "dddddd");

                        // student2Model 不受影响
                        Student2Model.Entity id = student2Model.newQuery()
                            .where("id", "4")
                            .firstOrFail().toObject();
//                        Assert.assertEquals(id.getName(), "testttt");

                    }, 1);
                    StudentModel.Entity id = studentModel.newQuery()
                        .where("id", "9")
                        .firstOrFail().toObject();
//                    Assert.assertEquals(id.getName(), "testttt");
                }, 3);

                countDownLatch.countDown();
                System.out.println("子线程结束");
            }).start();
            System.out.println("开启线程: " + i);
        }
        countDownLatch.await();
        System.out.println("所有线程结束");
    }

}
