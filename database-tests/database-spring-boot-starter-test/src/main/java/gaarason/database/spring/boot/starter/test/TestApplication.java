package gaarason.database.spring.boot.starter.test;

import gaarason.database.spring.boot.starter.annotation.GaarasonDatabaseScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.concurrent.CountDownLatch;

@SpringBootApplication
@GaarasonDatabaseScan({"gaarason.database.spring.boot.starter","gaarason.database.spring"})
@EnableTransactionManagement
public class TestApplication {
    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(TestApplication.class, args);
        new CountDownLatch(1).await();
    }
}