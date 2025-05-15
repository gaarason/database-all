package gaarason.database.contract.function;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.eloquent.RecordList;
import gaarason.database.contract.model.Event;
import gaarason.database.lang.Nullable;

@FunctionalInterface
public interface EventQueryEdFunctionalInterface<B extends Builder<B, T, K>, T, K> {

     /**
      *
      * @param eventProcessor 事件处理器
      * @param builder 查询构造器(快照)
      * @param record 查询结果集(快照)
      * @param records 查询结果集集合(快照)
      */
     void execute(Event<B, T, K> eventProcessor, Builder<B, T, K> builder, @Nullable Record<T, K> record, @Nullable RecordList<T, K> records);
}
