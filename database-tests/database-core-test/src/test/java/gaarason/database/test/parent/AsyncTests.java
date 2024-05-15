package gaarason.database.test.parent;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.test.models.normal.StudentCombination;
import gaarason.database.test.models.normal.StudentModel;
import gaarason.database.test.models.normal.StudentReversal;
import gaarason.database.test.parent.base.BaseTests;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
abstract public class AsyncTests extends BaseTests {

    private static final StudentModel studentModel = new StudentModel();

    private static final StudentReversal.Model studentReversalModel = new StudentReversal.Model();

    private static final StudentCombination studentCombination = new StudentCombination();

    @Override
    protected GaarasonDataSource getGaarasonDataSource() {
        return studentModel.getGaarasonDataSource();
    }

    @Override
    protected List<TABLE> getInitTables() {
        return Collections.singletonList(TABLE.student);
    }

    @Test
    public void 原生异步查询() throws ExecutionException, InterruptedException {
        int second = 1;
        long t1 = System.currentTimeMillis();
        CompletableFuture<Record<StudentModel.Entity, Integer>> future = studentModel.nativeQueryAsync(
            "select sleep(" + second + ")",
            null);
        long t2 = System.currentTimeMillis();
        future.get();

        long t3 = System.currentTimeMillis();
        System.out.println(t2 - t1);
        System.out.println(t3 - t2);
        Assert.assertTrue(t2 - t1 < 100);
        Assert.assertTrue(t3 - t2 > second * 1000);
        System.out.println("ok");
    }

    @Test
    public void 原生异步查询_超时控制() throws ExecutionException, InterruptedException {
        int second = 1;
        int timeout = 500; // ms
        long t1 = System.currentTimeMillis();
        CompletableFuture<Record<StudentModel.Entity, Integer>> future = studentModel.nativeQueryAsync(
            "select sleep(" + second + ")",
            null);
        long t2 = System.currentTimeMillis();
        Assert.assertThrows(TimeoutException.class, () -> {
            future.get(timeout, TimeUnit.MILLISECONDS);
        });

        long t3 = System.currentTimeMillis();
        Assert.assertTrue(t2 - t1 < 100);
        Assert.assertTrue(t3 - t2 < second * 1000);
        Assert.assertTrue(t3 - t2 > timeout);
        System.out.println(t2 - t1);
        System.out.println(t3 - t2);
        System.out.println("ok");
    }

    @Test
    public void 原生异步查询_并发() throws ExecutionException, InterruptedException {
        int second = 1;
        long t1 = System.currentTimeMillis();
        studentModel.nativeQueryAsync("select sleep(" + second + ")", null);
        studentModel.nativeQueryAsync("select sleep(" + second + ")", null);
        CompletableFuture<Record<StudentModel.Entity, Integer>> future = studentModel.nativeQueryAsync(
            "select sleep(" + second + ")", null);
        long t2 = System.currentTimeMillis();
        future.get().toObject();

        long t3 = System.currentTimeMillis();
        Assert.assertTrue(t2 - t1 < 100);
        Assert.assertTrue(t3 - t2 > second * 1000);
        System.out.println(t2 - t1);
        System.out.println(t3 - t2);
        System.out.println("ok");
    }

    @Test
    public void 原生异步查询_同步事务中_并发() throws ExecutionException, InterruptedException {
        int second = 1;
        long t1 = System.currentTimeMillis();
        // 在主线程上的感知, 会退化为同步执行
        CompletableFuture<Record<StudentModel.Entity, Integer>> future = studentModel.newQuery()
            .transaction(() -> {
                studentModel.nativeQueryAsync("select sleep(" + second + ")", null);
                studentModel.nativeQueryAsync("select sleep(" + second + ")", null);
                return studentModel.nativeQueryAsync("select sleep(" + second + ")", null);
            });

        long t2 = System.currentTimeMillis();
        future.get().toObject();

        long t3 = System.currentTimeMillis();
        System.out.println(t2 - t1);
        System.out.println(t3 - t2);
        Assert.assertTrue(t2 - t1 > second * 1000 * 3);
        Assert.assertTrue(t3 - t2 < 100);
        System.out.println("ok");
    }

    @Test
    public void 原生异步查询_异步事务中_并发() throws ExecutionException, InterruptedException {
        int second = 1;
        long t1 = System.currentTimeMillis();
        // 在主线程上的感知, 会退化为同步执行
        CompletableFuture<CompletableFuture<Record<StudentModel.Entity, Integer>>> future = studentModel.newQuery()
            .transactionAsync(() -> {
                studentModel.nativeQueryAsync("select sleep(" + second + ")", null);
                studentModel.nativeQueryAsync("select sleep(" + second + ")", null);
                return studentModel.nativeQueryAsync("select sleep(" + second + ")", null);
            }, 3);

        long t2 = System.currentTimeMillis();
        future.get().get().toObject();

        long t3 = System.currentTimeMillis();
        System.out.println(t2 - t1);
        System.out.println(t3 - t2);
        Assert.assertTrue(t2 - t1 < 1000);
        Assert.assertTrue(t3 - t2 > second * 1000 * 3);
        System.out.println("ok");
    }


    @Test
    public void 原生异步查询_异步事务中_并发2() throws ExecutionException, InterruptedException {
        int second = 1;
        long t1 = System.currentTimeMillis();
        CompletableFuture<Record<StudentModel.Entity, Integer>> future = studentModel.newQuery()
            .transactionAsync(() -> {
                studentModel.nativeQuery("select sleep(" + second + ")", null);
                studentModel.nativeQuery("select sleep(" + second + ")", null);
                return studentModel.nativeQueryOrFail("select sleep(" + second + ")", null);
            }, 3);

        long t2 = System.currentTimeMillis();
        future.get().toObject();

        long t3 = System.currentTimeMillis();
        System.out.println(t2 - t1);
        System.out.println(t3 - t2);
        Assert.assertTrue(t2 - t1 < 1000);
        Assert.assertTrue(t3 - t2 > second * 1000 * 3);
        System.out.println("ok");
    }

    @Test
    public void 原生异步查询_异步事务间_并发() throws ExecutionException, InterruptedException {
        int second = 1;
        long t1 = System.currentTimeMillis();
        CompletableFuture<Boolean> future0 = studentModel.newQuery()
            .transactionAsync(() -> {
                studentModel.nativeQuery("select sleep(" + second + ")", null);
                studentModel.nativeQuery("select sleep(" + second + ")", null);
                studentModel.nativeQueryOrFail("select sleep(" + second + ")", null);
            });

        long t2 = System.currentTimeMillis();

        CompletableFuture<Boolean> future1 = studentModel.newQuery()
            .transactionAsync(() -> {
                studentModel.nativeQuery("select sleep(" + second + ")", null);
                studentModel.nativeQuery("select sleep(" + second + ")", null);
                studentModel.nativeQueryOrFail("select sleep(" + second + ")", null);
            });

        long t3 = System.currentTimeMillis();
        future0.get();
        long t4 = System.currentTimeMillis();
        future1.get();
        long t5 = System.currentTimeMillis();
        System.out.println(t2 - t1);
        System.out.println(t3 - t2);
        System.out.println(t4 - t3);
        System.out.println(t5 - t4);
        Assert.assertTrue(t2 - t1 < 1000);  // 第一次比较慢一点
        Assert.assertTrue(t3 - t2 < 100);
        Assert.assertTrue(t4 - t3 > second * 1000 * 3);
        Assert.assertTrue(t5 - t4 < 100);
        System.out.println("ok");
    }

    @Test
    public void 异步事务() throws ExecutionException, InterruptedException {

        String name0 = studentModel.newQuery().findOrFail(1).toObject().getName();

        String newName = "new name";

        Assert.assertNotEquals(name0, newName);

        CompletableFuture<Boolean> future = studentModel.newQuery()
            .transactionAsync(() -> {
                Record<StudentModel.Entity, Integer> record = studentModel.newQuery().findOrFail(1);
                StudentModel.Entity student = record.getEntity();
                student.setName(newName);
                return record.save();
            });

        String name1 = studentModel.newQuery().findOrFail(1).toObject().getName();

        future.get();

        String name2 = studentModel.newQuery().findOrFail(1).toObject().getName();

        Assert.assertEquals(name0, name1);

        Assert.assertEquals(newName, name2);
    }
}
