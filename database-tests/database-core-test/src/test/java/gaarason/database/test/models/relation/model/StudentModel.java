package gaarason.database.test.models.relation.model;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.test.config.MySqlBuilderV2;
import gaarason.database.test.models.relation.model.base.BaseModel;
import gaarason.database.test.models.relation.pojo.Student;
import gaarason.database.test.models.relation.pojo.Teacher;

public class StudentModel extends BaseModel<Student, Long> {


    @Override
    protected MySqlBuilderV2<Student, Long> apply(MySqlBuilderV2<Student, Long> builder) {
//        return builder.whereHas("teacher", subBuilder -> subBuilder.whereRaw("1"));
//        return builder.whereRaw("1");
        Builder<?, ? super Teacher, ?> teacherBuilder = getModelShadow().getByEntityClass(Teacher.class)
                .getModel().newQueryWithoutApply();
        return builder.andWhere(
                builder1 -> builder1.whereIn("id",
                                builder2 -> teacherBuilder.select("id"))
                        .orWhere(builder3 -> builder3.whereRaw("1")));
    }

    /**
     * 是否启用软删除
     */
    protected boolean softDeleting() {
        return true;
    }

    /**
     * 软删除查询作用域(反)
     * @param builder 查询构造器
     */
    @Override
    protected void scopeSoftDeleteOnlyTrashed(Builder<?, Student, Long> builder) {
        builder.where(DEFAULT_SOFT_DELETED_COLUMN_NAME, DEFAULT_SOFT_DELETED_VALUE_YSE);
    }

    /**
     * 软删除查询作用域(全)
     * @param builder 查询构造器
     */
    @Override
    protected void scopeSoftDeleteWithTrashed(Builder<?, Student, Long> builder) {


    }

    /**
     * 软删除查询作用域
     * @param builder 查询构造器
     */
    @Override
    protected void scopeSoftDelete(Builder<?, Student, Long> builder) {
        builder.where(DEFAULT_SOFT_DELETED_COLUMN_NAME, DEFAULT_SOFT_DELETED_VALUE_NO);
    }

    /**
     * 软删除实现
     * @param builder 查询构造器
     * @return 删除的行数
     */
    @Override
    protected int softDelete(Builder<?, Student, Long> builder) {
        return builder.data(DEFAULT_SOFT_DELETED_COLUMN_NAME, DEFAULT_SOFT_DELETED_VALUE_YSE).update();
    }

    /**
     * 恢复软删除实现
     * @param builder 查询构造器
     * @return 恢复的行数
     */
    @Override
    protected int softDeleteRestore(Builder<?, Student, Long> builder) {
        return builder.data(DEFAULT_SOFT_DELETED_COLUMN_NAME, DEFAULT_SOFT_DELETED_VALUE_NO).update();
    }
}

