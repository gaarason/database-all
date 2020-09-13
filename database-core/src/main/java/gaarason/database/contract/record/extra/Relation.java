package gaarason.database.contract.record.extra;

import gaarason.database.contract.function.GenerateSqlPart;
import gaarason.database.eloquent.Record;
import gaarason.database.eloquent.RecordList;

import java.util.Collection;

public interface Relation {

    boolean attach(Record<?, ?> targetRecord);

    boolean detach(Record<?, ?> targetRecord);

    boolean sync(RecordList<?, ?> targetRecords);

    boolean syncWithoutDetaching(RecordList<?, ?> targetRecords);

    boolean toggle(RecordList<?, ?> targetRecords);


    boolean attach(String id);

    boolean detach(String id);

    boolean sync(Collection<String> ids);

    boolean syncWithoutDetaching(Collection<String> ids);

    boolean toggle(Collection<String> ids);


    boolean attach(Record<?, ?> targetRecord, GenerateSqlPart closure);

    boolean detach(Record<?, ?> targetRecord, GenerateSqlPart closure);

    boolean sync(RecordList<?, ?> targetRecords, GenerateSqlPart closure);

    boolean syncWithoutDetaching(RecordList<?, ?> targetRecords, GenerateSqlPart closure);

    boolean toggle(RecordList<?, ?> targetRecords, GenerateSqlPart closure);


    boolean attach(String id, GenerateSqlPart closure);

    boolean detach(String id, GenerateSqlPart closure);

    boolean sync(Collection<String> ids, GenerateSqlPart closure);

    boolean syncWithoutDetaching(Collection<String> ids, GenerateSqlPart closure);

    boolean toggle(Collection<String> ids, GenerateSqlPart closure);
}
