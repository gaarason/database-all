package gaarason.database.test.models.normal;

import gaarason.database.annotation.Column;
import gaarason.database.annotation.Primary;
import gaarason.database.annotation.Table;
import gaarason.database.test.models.normal.base.SingleModel;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

public class DatetimeTestModel extends SingleModel<DatetimeTestModel.Entity, Integer> {

    @Override
    protected boolean softDeleting() {
        return true;
    }

    @Data
    @Table(name = "datetime_test")
    public static class Entity implements Serializable {

        private static final long serialVersionUID = 1L;

        @Primary
        private Integer id;

        @Column(length = 20)
        private String name;

        @Column(name = "time_column")
        private LocalTime timeColumn;

        @Column(name = "date_column")
        private LocalDate dateColumn;

        @Column(name = "datetime_column")
        private LocalDateTime datetimeColumn;

        @Column(name = "timestamp_column")
        private Date timestampColumn;

        private boolean isDeleted;

    }
}
