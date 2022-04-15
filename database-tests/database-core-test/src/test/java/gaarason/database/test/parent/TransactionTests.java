package gaarason.database.test.parent;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.test.models.normal.Student2Model;
import gaarason.database.test.models.normal.Student3Model;
import gaarason.database.test.models.normal.StudentModel;
import gaarason.database.test.parent.base.BaseTests;
import gaarason.database.test.utils.MultiThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.concurrent.CountDownLatch;

/**
 * `传播性`为同数据库连接不可嵌套, 不同的数据库连接可以任意嵌套
 */
@Slf4j
@FixMethodOrder(MethodSorters.JVM)
abstract public class TransactionTests extends BaseTests {

    /*********  以下分别使用不同的数据库连接, 理解成物理上不同的数据库即可  ***********/
    protected static StudentModel studentModel = new StudentModel();

    protected static Student2Model student2Model = new Student2Model();

    protected static Student3Model student3Model = new Student3Model();

    @Override
    protected GaarasonDataSource getGaarasonDataSource(){
        return studentModel.getGaarasonDataSource();
    }

    @Test
    public void 事物_单个数据连接可嵌套事物_子事务独立提交() {
        // 1层事物
        studentModel.newQuery().transaction(() -> {
            studentModel.newQuery().where("id", "4").data("name", "44444").update();

            // 2层事物
            studentModel.newQuery().transaction(() -> {
                studentModel.newQuery().where("id", "3").data("name", "33333").update();

                try {
                    // 3层事物
                    studentModel.newQuery().transaction(() -> {
                        studentModel.newQuery().where("id", "2").data("name", "22222").update();

                        try {
                            // 4层事物
                            studentModel.newQuery().transaction(() -> {

                                studentModel.newQuery().where("id", "1").data("name", "11111").update();
                                StudentModel.Entity entity = studentModel.newQuery()
                                    .where("id", "1")
                                    .firstOrFail()
                                    .toObject();
                                Assert.assertEquals(entity.getName(), "11111");
                                throw new RuntimeException("业务上抛了个异常");

                            }, 1);

                        } catch (RuntimeException e) {
                        }
                        StudentModel.Entity entity = studentModel.findOrFail(1).toObject();
                        Assert.assertNotEquals(entity.getName(), "11111");

                        StudentModel.Entity entity1 = studentModel.findOrFail(2).toObject();
                        Assert.assertEquals(entity1.getName(), "22222");
                        throw new RuntimeException("业务上抛了个异常22");
                    }, 1);

                } catch (RuntimeException e) {

                }
                StudentModel.Entity entity1 = studentModel.findOrFail(2).toObject();
                Assert.assertNotEquals(entity1.getName(), "22222");

                StudentModel.Entity entity11 = studentModel.findOrFail(3).toObject();
                Assert.assertEquals(entity11.getName(), "33333");
            }, 1);

            StudentModel.Entity entity11 = studentModel.findOrFail(3).toObject();
            Assert.assertEquals(entity11.getName(), "33333");

            StudentModel.Entity entity111 = studentModel.findOrFail(4).toObject();
            Assert.assertEquals(entity111.getName(), "44444");
        }, 3);

        StudentModel.Entity entity111 = studentModel.findOrFail(4).toObject();
        Assert.assertEquals(entity111.getName(), "44444");
    }


//    @Test(expected = NestedTransactionException.class)
//    public void 事物_单个数据连接不可嵌套事物() {
//        // 1层事物
//        studentModel.newQuery().transaction(() -> {
//            // 2层事物 应该抛出异常 NestedTransactionException
//            return studentModel.newQuery().transaction(() -> {
//                // 3层事物
//                return studentModel.newQuery().transaction(() -> {
//                    try {
//                        // 4层事物
//                        return studentModel.newQuery().transaction(() -> {
//                            studentModel.newQuery().where("id", "1").data("name", "dddddd").update();
//                            StudentModel.Entity entity = studentModel.newQuery()
//                                .where("id", "1")
//                                .firstOrFail()
//                                .toObject();
//                            Assert.assertEquals(entity.getName(), "dddddd");
//                            throw new RuntimeException("业务上抛了个异常");
//                        }, 1);
//                    } catch (RuntimeException ignored) {
//                    }
//                    return true;
//                }, 1);
//            }, 1);
//        }, 3);
//    }

    @Test
    public void 事物_多线程下_多个数据连接嵌套事物2() throws InterruptedException {

        MultiThreadUtil.run(100, 3, () -> {
            System.out.println("子线程开启 ------------ " + Thread.currentThread().getName());
            try {
                // 第1层事物
                studentModel.newQuery().transaction(() -> {
                    try {
                        studentModel.newQuery().data("name", "testttt").where("id", "9").update();
                    } catch (Throwable e) {
                        log.error("不应该出现的异常", e);
                        throw e;
                    }
                    // 第2层事物 因为是不同的数据库连接,所以正常开启
                    student2Model.newQuery().transaction(() -> {
                        student2Model.newQuery().data("name", "testttt").where("id", "4").update();
                        try {
                            // 第3层事物 因为是不同的数据库连接,所以正常开启
                            student3Model.newQuery().transaction(() -> {
                                student3Model.newQuery().where("id", "1").data("name", "dddddd").update();
                                Student3Model.Entity entity = student3Model.newQuery()
                                    .where("id", "1")
                                    .firstOrFail()
                                    .toObject();
//                                try {
//                                    Thread.sleep(2);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
                                throw new RuntimeException("业务上抛了个异常");
                            }, 1);
                            // 第3层事物 结束
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

                    //  第2层事物结束
                    StudentModel.Entity id = studentModel.newQuery()
                        .where("id", "9")
                        .firstOrFail().toObject();
                }, 3);

                // 第1层事物结束
            } finally {
                System.out.println("子线程结束 " + Thread.currentThread().getName());
            }
        });

    }


    @Test
    public void 事物_在事物中开启多线程执行_子进程不处于事物中() {

        studentModel.newQuery().data("name", "vv").where("id", "9").update();
        // 第1层事物
        studentModel.newQuery().transaction(() -> {

            // 因为事物状态 绑定到了线程 所以先开启事物,然后在事物中开启多个子线程,这些子进程是不处于事物中的!
            MultiThreadUtil.run(100, 3, () -> {
                System.out.println("子线程开启 ------------ " + Thread.currentThread().getName());
                try {
                    studentModel.newQuery().data("name", "tesxxt").where("id", "9").update();
                    StudentModel.Entity entity = studentModel.newQuery()
                        .where("id", "9")
                        .firstOrFail()
                        .toObject();
                    Assert.assertEquals(entity.getName(), "tesxxt");
                    throw new RuntimeException("业务上抛了个异常");
                    // 第1层事物结束
                } finally {
                    System.out.println("子线程结束 " + Thread.currentThread().getName());
                }
            });
        }, 1);

        StudentModel.Entity entity = studentModel.newQuery()
            .where("id", "9")
            .firstOrFail()
            .toObject();
        Assert.assertEquals(entity.getName(), "tesxxt");
//        Assert.assertEquals(entity.getName(), "vv");
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
        int count = 100;
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

}
