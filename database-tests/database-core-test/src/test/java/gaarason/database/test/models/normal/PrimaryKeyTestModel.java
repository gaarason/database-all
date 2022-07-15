package gaarason.database.test.models.normal;

import gaarason.database.annotation.Column;
import gaarason.database.annotation.Primary;
import gaarason.database.annotation.Table;
import gaarason.database.contract.support.IdGenerator;
import gaarason.database.test.models.normal.base.SingleModel;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class PrimaryKeyTestModel extends SingleModel<PrimaryKeyTestModel.Entity, Integer> {

    @Override
    protected boolean softDeleting() {
        return true;
    }

    @Data
    @Table(name = "null_test")
    public static class Entity implements Serializable {

        private static final long serialVersionUID = 1L;

        @Primary(idGenerator = CustomPrimaryKey.class)
        private Integer id;

        @Column(length = 20, nullable = true)
        private String name;

        @Column(name = "time_column", nullable = true)
        private LocalTime timeColumn;

        @Column(name = "date_column", nullable = true)
        private LocalDate dateColumn;

        @Column(name = "datetime_column", nullable = true)
        private LocalDateTime datetimeColumn;

        @Column(name = "timestamp_column", nullable = true)
        private Date timestampColumn;

        private boolean isDeleted;

    }

    public static class CustomPrimaryKey implements IdGenerator<Integer> {

        private final static AtomicInteger id = new AtomicInteger(200);

        @Override
        public Integer nextId() {
            return id.getAndIncrement();
        }
    }
}
