package gaarason.database.contract.function;

import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.model.Event;

@FunctionalInterface
public interface EventQueryIngFunctionalInterface<B extends Builder<B, T, K>, T, K> {

     void execute(Event<B, T, K> eventProcessor);
}
