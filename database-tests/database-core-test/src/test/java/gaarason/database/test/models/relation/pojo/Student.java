package gaarason.database.test.models.relation.pojo;

import gaarason.database.annotation.*;
import gaarason.database.annotation.base.Relation;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.function.GenerateSqlPartFunctionalInterface;
import gaarason.database.contract.support.FieldStrategy;
import gaarason.database.core.Container;
import gaarason.database.eloquent.relation.BaseRelationSubQuery;
import gaarason.database.eloquent.relation.HasOneOrManyQueryRelation;
import gaarason.database.lang.Nullable;
import gaarason.database.provider.ModelShadowProvider;
import gaarason.database.test.models.relation.model.RelationshipStudentTeacherModel;
import gaarason.database.test.models.relation.pojo.base.BaseEntity;
import gaarason.database.util.ObjectUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Table(name = "student")
public class Student extends BaseEntity implements Serializable {

    final public static String NAME = "name";
    final public static String AGE = "age";
    final public static String SEX = "sex";
    final public static String TEACHER_ID = "teacher_id";
    final public static String IS_DELETED = "is_deleted";
    final public static String CREATED_AT = "created_at";
    final public static String UPDATED_AT = "updated_at";

    @Column(name = "name", length = 20L, comment = "姓名")
    private String name;

    @Column(name = "age", unsigned = true, comment = "年龄")
    private Integer age;

    @Column(name = "sex", unsigned = true, comment = "性别1男2女")
    private Integer sex;

    @Column(name = "teacher_id", unsigned = true, comment = "教师id")
    private Long teacherId;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "created_at", insertStrategy = FieldStrategy.Never.class, updateStrategy = FieldStrategy.Never.class, comment = "新增时间")
    private Date createdAt;

    @Column(name = "updated_at", insertStrategy = FieldStrategy.Never.class, updateStrategy = FieldStrategy.Never.class, comment = "更新时间")
    private Date updatedAt;

    @BelongsTo(localModelForeignKey = "teacher_id", parentModelLocalKey = "id")
    private Teacher teacher;

    @HasOneOrMany(sonModelForeignKey = "student_id")
    private List<RelationshipStudentTeacher> relationshipStudentTeachers;


    @BelongsToMany(relationModel = RelationshipStudentTeacherModel.class,
        foreignKeyForLocalModel = "student_id", foreignKeyForTargetModel = "teacher_id", localModelLocalKey = "id",
        targetModelLocalKey = "id")
    private List<Teacher> teachersBelongsToMany;

    @HasOne(sonModelForeignKey = "student_id")
    private RelationshipStudentTeacher relationshipStudentTeacher;

    // ------------------------------------ --------------------------------------------//


    @Documented
    @Inherited
    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @Relation(HasOneQueryRelation.class)
    public @interface HasOne {
        /**
         * `子表`中的`关联本表的外键`
         * @return `子表`中的`关联本表的外键`
         */
        String sonModelForeignKey();

        /**
         * `本表`中的`关联键`, 默认值为`本表`的主键(`@Primary()`修饰的键)
         * @return `本表`中的`关联键`
         */
        String localModelLocalKey() default "";

    }


    /**
     * @see HasOneOrManyQueryRelation
     */
    public static class HasOneQueryRelation extends BaseRelationSubQuery {

        private final HasOneQueryRelation.HasOneTemplate hasOneTemplate;

        /**
         * 目标模型外键的默认值, 仅在解除关系时使用
         */
        @Nullable
        private final Object defaultSonModelForeignKeyValue;

        public HasOneQueryRelation(Field field, ModelShadowProvider modelShadowProvider, Model<?, ?> model) {
            super(modelShadowProvider, model);
            hasOneTemplate = new HasOneQueryRelation.HasOneTemplate(field);

            defaultSonModelForeignKeyValue = modelShadowProvider.get(hasOneTemplate.sonModel)
                .getEntityMember()
                .getFieldMemberByColumnName(hasOneTemplate.sonModelForeignKey)
                .getDefaultValue();
        }

        @Override
        public Builder<?, ?>[] prepareBuilderArr(boolean relationOperation, List<Map<String, Object>> originalMetadataMapList,
            GenerateSqlPartFunctionalInterface<?, ?> generateSqlPart) {
            return new Builder<?, ?>[]{null,
                generateSqlPart.execute(ObjectUtils.typeCast(hasOneTemplate.sonModel.newQuery())).whereIn(
                    hasOneTemplate.sonModelForeignKey,
                    getColumnInMapList(originalMetadataMapList, hasOneTemplate.localModelLocalKey))};
        }

        @Override
        public RecordList<?, ?> dealBatchForTarget(boolean relationOperation, Builder<?, ?> builderForTarget,
            RecordList<?, ?> relationRecordList) {
            return hasOneTemplate.sonModel.newQuery().setBuilder(ObjectUtils.typeCast(builderForTarget)).get();
        }

        @Override
        public List<Object> filterBatchRecord(boolean relationOperation, Record<?, ?> theRecord, RecordList<?, ?> targetRecordList,
            Map<String, RecordList<?, ?>> cacheRelationRecordList) {
            // 子表的外键字段名
            String column = hasOneTemplate.sonModelForeignKey;
            // 本表的关系键值
            Object value = theRecord.getMetadataMap().get(hasOneTemplate.localModelLocalKey);

            assert value != null;
            return findObjList(targetRecordList.toObjectList(cacheRelationRecordList), column, value);
        }

        @Override
        public int attach(Record<?, ?> theRecord, RecordList<?, ?> targetRecords, Map<String, Object> relationDataMap) {
            return attach(theRecord, getTargetRecordPrimaryKeyIds(targetRecords), relationDataMap);
        }

        @Override
        public int attach(Record<?, ?> theRecord, Collection<Object> targetPrimaryKeyValues,
            Map<String, Object> relationDataMap) {
            if (targetPrimaryKeyValues.isEmpty()) {
                return 0;
            }

            // 关联键值(当前表关系键(默认当前表主键))(子表外键)
            Object relationKeyValue = theRecord.getMetadataMap().get(hasOneTemplate.localModelLocalKey);

            // 执行更新
            assert relationKeyValue != null;
            return hasOneTemplate.sonModel.newQuery()
                .whereIn(hasOneTemplate.sonModel.getPrimaryKeyColumnName(), targetPrimaryKeyValues)
                .where(hasOneTemplate.sonModelForeignKey, "!=", relationKeyValue)
                .data(hasOneTemplate.sonModelForeignKey, relationKeyValue)
                .update();
        }

        @Override
        public int detach(Record<?, ?> theRecord) {
            // 关联键值(当前表关系键(默认当前表主键))(子表外键)
            Object relationKeyValue = theRecord.getMetadataMap().get(hasOneTemplate.localModelLocalKey);

            // 执行更新
            // 目标,必须是关联关系, 才解除
            return hasOneTemplate.sonModel.newQuery()
                .where(hasOneTemplate.sonModelForeignKey, relationKeyValue)
                .data(hasOneTemplate.sonModelForeignKey, defaultSonModelForeignKeyValue)
                .update();
        }

        @Override
        public int detach(Record<?, ?> theRecord, RecordList<?, ?> targetRecords) {
            // 应该更新的子表的主键列表
            List<Object> targetRecordPrimaryKeyIds = targetRecords.toList(
                recordTemp -> recordTemp.getMetadataMap().get(recordTemp.getModel().getPrimaryKeyColumnName()));
            return detach(theRecord, targetRecordPrimaryKeyIds);
        }

        @Override
        public int detach(Record<?, ?> theRecord, Collection<Object> targetPrimaryKeyValues) {
            if (targetPrimaryKeyValues.isEmpty()) {
                return 0;
            }

            // 关联键值(当前表关系键(默认当前表主键))(子表外键)
            Object relationKeyValue = theRecord.getMetadataMap().get(hasOneTemplate.localModelLocalKey);

            // 执行更新
            // 目标,必须是关联关系, 才解除
            return hasOneTemplate.sonModel.newQuery()
                .whereIn(hasOneTemplate.sonModel.getPrimaryKeyColumnName(), targetPrimaryKeyValues)
                .where(hasOneTemplate.sonModelForeignKey, relationKeyValue)
                .data(hasOneTemplate.sonModelForeignKey, defaultSonModelForeignKeyValue)
                .update();
        }

        @Override
        public int sync(Record<?, ?> theRecord, RecordList<?, ?> targetRecords, Map<String, Object> relationDataMap) {
            return sync(theRecord, getTargetRecordPrimaryKeyIds(targetRecords), relationDataMap);
        }

        @Override
        public int sync(Record<?, ?> theRecord, Collection<Object> targetPrimaryKeyValues,
            Map<String, Object> relationDataMap) {
            // 关联键值(当前表关系键(默认当前表主键))(子表外键)
            Object relationKeyValue = theRecord.getMetadataMap().get(hasOneTemplate.localModelLocalKey);
            assert relationKeyValue != null;

            return hasOneTemplate.sonModel.newQuery().transaction(() -> {
                // 现存的关联关系, 不需要据需存在的, 解除
                int detachNum = hasOneTemplate.sonModel.newQuery()
                    .whereNotIn(hasOneTemplate.sonModel.getPrimaryKeyColumnName(), targetPrimaryKeyValues)
                    .where(hasOneTemplate.sonModelForeignKey, relationKeyValue)
                    .data(hasOneTemplate.sonModelForeignKey, defaultSonModelForeignKeyValue)
                    .update();

                // 执行更新
                int attachNum = hasOneTemplate.sonModel.newQuery()
                    .whereIn(hasOneTemplate.sonModel.getPrimaryKeyColumnName(), targetPrimaryKeyValues)
                    .where(hasOneTemplate.sonModelForeignKey, "!=", relationKeyValue)
                    .data(hasOneTemplate.sonModelForeignKey, relationKeyValue)
                    .update();

                return detachNum + attachNum;
            });
        }

        @Override
        public int toggle(Record<?, ?> theRecord, RecordList<?, ?> targetRecords, Map<String, Object> relationDataMap) {
            return toggle(theRecord, getTargetRecordPrimaryKeyIds(targetRecords), relationDataMap);
        }

        @Override
        public int toggle(Record<?, ?> theRecord, Collection<Object> targetPrimaryKeyValues,
            Map<String, Object> relationDataMap) {
            if (targetPrimaryKeyValues.isEmpty()) {
                return 0;
            }

            // 关联键值(当前表关系键(默认当前表主键))(子表外键)
            Object relationKeyValue = theRecord.getMetadataMap().get(hasOneTemplate.localModelLocalKey);

            return hasOneTemplate.sonModel.newQuery().transaction(() -> {
                // 现存的关联关系 主键值集合
                List<Object> alreadyExistSonModelPrimaryKeyValues = hasOneTemplate.sonModel.newQuery()
                    .select(hasOneTemplate.sonModel.getPrimaryKeyColumnName())
                    .whereIn(hasOneTemplate.sonModel.getPrimaryKeyColumnName(), targetPrimaryKeyValues)
                    .where(hasOneTemplate.sonModelForeignKey, relationKeyValue)
                    .get()
                    .toOneColumnList();

                // 现存的关联关系, 解除关系
                int detachNum = hasOneTemplate.sonModel.newQuery()
                    .whereIn(hasOneTemplate.sonModel.getPrimaryKeyColumnName(), targetPrimaryKeyValues)
                    .where(hasOneTemplate.sonModelForeignKey, relationKeyValue)
                    .data(hasOneTemplate.sonModelForeignKey, defaultSonModelForeignKeyValue)
                    .update();

                // 需要增加的关系 主键值集合
                Collection<Object> compatibleTargetPrimaryKeyValues = compatibleCollection(targetPrimaryKeyValues,
                    hasOneTemplate.sonModel);
                compatibleTargetPrimaryKeyValues.removeAll(alreadyExistSonModelPrimaryKeyValues);

                // 不存在的关系, 新增关系
                int attachNum = !compatibleTargetPrimaryKeyValues.isEmpty() ? hasOneTemplate.sonModel.newQuery()
                    .whereIn(hasOneTemplate.sonModel.getPrimaryKeyColumnName(), compatibleTargetPrimaryKeyValues)
                    .data(hasOneTemplate.sonModelForeignKey, relationKeyValue)
                    .update() : 0;

                return detachNum + attachNum;
            });
        }

        @Override
        protected Container getContainer() {
            return hasOneTemplate.sonModel.getGaarasonDataSource().getContainer();
        }

        class HasOneTemplate {

            final Model<?, ?> sonModel;

            final String sonModelForeignKey;

            final String localModelLocalKey;

            HasOneTemplate(Field field) {
                HasOne hasOne = field.getAnnotation(HasOne.class);
                sonModel = getModelInstance(field);

                sonModelForeignKey = hasOne.sonModelForeignKey();
                localModelLocalKey = "".equals(hasOne.localModelLocalKey()) ? getPrimaryKeyColumnName(sonModel) :
                    hasOne.localModelLocalKey();

            }
        }

    }
}
