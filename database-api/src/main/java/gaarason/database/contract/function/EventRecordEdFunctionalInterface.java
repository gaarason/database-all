package gaarason.database.contract.function;

import gaarason.database.contract.eloquent.Record;
import gaarason.database.contract.model.Event;

@FunctionalInterface
public interface EventRecordEdFunctionalInterface<T, K> {

     void execute(Event<?, T, K> eventProcessor, Record<T, K> record);
}
