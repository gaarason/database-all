package gaarason.database.test.parent;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.test.models.normal.DatetimeTestModel;
import gaarason.database.test.parent.base.BaseTests;
import gaarason.database.util.LocalDateUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.sql.DataSource;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
abstract public class LocalDateTests extends BaseTests {

    protected static DatetimeTestModel datetimeTestModel = new DatetimeTestModel();

    protected GaarasonDataSource getGaarasonDataSource(){
        return datetimeTestModel.getGaarasonDataSource();
    }

    @Test
    public void 查询结果映射到实体对象() {
        final List<DatetimeTestModel.Entity> entities = datetimeTestModel.newQuery().get().toObjectList();
        for (DatetimeTestModel.Entity entity : entities) {
            Assert.assertNotNull(entity.getTimeColumn());
            Assert.assertNotNull(entity.getDateColumn());
            Assert.assertNotNull(entity.getDatetimeColumn());
            Assert.assertNotNull(entity.getTimestampColumn());
        }

        final DatetimeTestModel.Entity entity = datetimeTestModel.findOrFail(1).toObject();
        log.info(entity.toString());
        Assert.assertEquals(entity.getTimeColumn(), LocalTime.parse("17:15:23", DateTimeFormatter.ofPattern("HH:mm:ss")));
        Assert.assertEquals(entity.getDateColumn(), LocalDate.parse("2010-04-24", DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        Assert.assertTrue(entity.getDatetimeColumn().isEqual(LocalDateUtils.str2LocalDateTime("2009-03-14 17:15:23")));
        Assert.assertEquals(entity.getTimestampColumn().getTime(),
            LocalDateUtils.localDateTime2date(LocalDateUtils.str2LocalDateTime("2010-04-24 22:11:03")).getTime());
    }

    @Test
    public void ORM插入() {
        final Record<DatetimeTestModel.Entity, Integer> newRecord = datetimeTestModel.newRecord();
        // 因为数据库 没有存储毫秒, 所以在这儿吧毫秒置为 0
        final LocalDateTime localDateTime = LocalDateTime.now().withNano(0);
        final long old = localDateTime.toEpochSecond(ZoneOffset.of("+8"));
        final int id = 33;
        final String name = "xxxx";

        final DatetimeTestModel.Entity entity = newRecord.getEntity();
        entity.setId(id);
        entity.setName(name);
        entity.setDatetimeColumn(localDateTime);
        entity.setDateColumn(localDateTime.toLocalDate());
        entity.setTimeColumn(localDateTime.toLocalTime());
        entity.setTimestampColumn(LocalDateUtils.localDateTime2date(localDateTime));
        newRecord.save();

        // 通过查询来检测
        final Record<DatetimeTestModel.Entity, Integer> theRecord = datetimeTestModel.findOrFail(id);
        final DatetimeTestModel.Entity stu = theRecord.toObject();
        Assert.assertEquals(old, localDateTime.toEpochSecond(ZoneOffset.of("+8")));
        Assert.assertEquals(stu.getName(), name);
        Assert.assertEquals(stu.getDateColumn(), localDateTime.toLocalDate());
        Assert.assertEquals(stu.getTimeColumn(), localDateTime.toLocalTime());
        Assert.assertEquals(stu.getDatetimeColumn(), localDateTime);
        Assert.assertEquals(stu.getTimestampColumn().getTime(), LocalDateUtils.localDateTime2date(localDateTime).getTime());
    }

    @Test
    public void t(){
        ModelShadowProvider.ModelInfo<Serializable, Serializable> datetimeTest = ModelShadowProvider.getByTableName("datetime_test");
        Set<String> strings = datetimeTest.getColumnFieldMap().keySet();
        Assert.assertTrue(strings.contains("time_column"));
        Assert.assertTrue(strings.contains("date_column"));
    }
}
