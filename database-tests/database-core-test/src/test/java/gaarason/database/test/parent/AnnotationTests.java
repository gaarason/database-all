package gaarason.database.test.parent;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.test.models.normal.AnnotationTestModel;
import gaarason.database.test.parent.base.BaseTests;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
abstract public class AnnotationTests extends BaseTests {

    protected static AnnotationTestModel annotationTestModel = new AnnotationTestModel();

    protected GaarasonDataSource getGaarasonDataSource() {
        return annotationTestModel.getGaarasonDataSource();
    }

    @Test
    public void primaryKey_自定义主键生成() {
        final Record<AnnotationTestModel.PrimaryKeyEntity, Integer> record0 = annotationTestModel.newRecord();
        record0.save();
        Assert.assertEquals(200, record0.getEntity().getId().intValue());

        final Record<AnnotationTestModel.PrimaryKeyEntity, Integer> record1 = annotationTestModel.newRecord();
        record1.save();
        Assert.assertEquals(201, record1.getEntity().getId().intValue());

        final Record<AnnotationTestModel.PrimaryKeyEntity, Integer> record2 = annotationTestModel.newRecord();
        record2.save();
        Assert.assertEquals(202, record2.getEntity().getId().intValue());

        final Record<AnnotationTestModel.PrimaryKeyEntity, Integer> record3 = annotationTestModel.newRecord();
        record3.save();
        Assert.assertEquals(203, record3.getEntity().getId().intValue());
    }

    @Test
    public void 自定义序列化_枚举值() {
        String name = "test_people";
        AnnotationTestModel.EnumEntity entity = new AnnotationTestModel.EnumEntity();
        entity.setSex(AnnotationTestModel.Sex.WOMAN);
        entity.setName(name);
        Integer id = annotationTestModel.newQuery().from(entity).insertGetId(entity);

        AnnotationTestModel.EnumEntity resultEntity = annotationTestModel.newQuery()
            .from(entity)
            .findOrFail(id)
            .toObject(AnnotationTestModel.EnumEntity.class);

        Assert.assertEquals(name, resultEntity.getName());
        Assert.assertEquals(AnnotationTestModel.Sex.WOMAN, resultEntity.getSex());
    }

}
