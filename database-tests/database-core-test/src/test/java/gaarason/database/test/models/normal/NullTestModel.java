package gaarason.database.test.models.normal;

import gaarason.database.annotation.Column;
import gaarason.database.annotation.Primary;
import gaarason.database.annotation.Table;
import gaarason.database.appointment.FieldStrategy;
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

        private static final long serialVersionUID = 1L;

        @Primary
        private Integer id;

        @Column(length = 20, strategy = FieldStrategy.ALWAYS)
        private String name;

        @Column(name = "time_column", strategy = FieldStrategy.ALWAYS)
        private LocalTime timeColumn;

        @Column(name = "date_column", strategy = FieldStrategy.ALWAYS)
        private LocalDate dateColumn;

        @Column(name = "datetime_column", strategy = FieldStrategy.ALWAYS)
        private LocalDateTime datetimeColumn;

        @Column(name = "timestamp_column", strategy = FieldStrategy.ALWAYS)
        private Date timestampColumn;

        private boolean isDeleted;

    }
}
