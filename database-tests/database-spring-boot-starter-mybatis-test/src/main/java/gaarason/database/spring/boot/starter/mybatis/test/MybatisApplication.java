package gaarason.database.spring.boot.starter.mybatis.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.concurrent.CountDownLatch;

@SpringBootApplication
@EnableTransactionManagement
public class MybatisApplication {

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(MybatisApplication.class, args);
        new CountDownLatch(1).await();
    }



}