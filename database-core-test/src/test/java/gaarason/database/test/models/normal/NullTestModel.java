package gaarason.database.test.models.normal;

import gaarason.database.eloquent.annotation.Column;
import gaarason.database.eloquent.annotation.Primary;
import gaarason.database.eloquent.annotation.Table;
import gaarason.database.test.models.normal.base.SingleModel;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

public class NullTestModel extends SingleModel<NullTestModel.Entity, Integer> {

    @Override
    protected boolean softDeleting() {
        return true;
    }

    @Data
    @Table(name = "null_test")
    public static class Entity implements Serializable {

        @Primary
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
}
