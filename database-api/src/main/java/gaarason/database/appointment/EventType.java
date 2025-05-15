package gaarason.database.appointment;

/**
 * 事件类型
 * @author xt
 */
public class EventType {

    public enum QueryIng {
        eventQueryRetrieving,
        eventQueryCreating,
        eventQueryUpdating,
        eventQueryDeleting,
        eventQueryForceDeleting,
        eventQueryRestoring,
    }

    public enum QueryEd {
        eventQueryRetrieved,
        eventQueryCreated,
        eventQueryUpdated,
        eventQueryDeleted,
        eventQueryForceDeleted,
        eventQueryRestored,
    }

    public enum RecordIng {
        eventRecordSaving,
        eventRecordCreating,
        eventRecordUpdating,
        eventRecordDeleting,
        eventRecordForceDeleting,
        eventRecordRestoring,
    }

    public enum RecordEd {
        eventRecordRetrieved,
        eventRecordCreated,
        eventRecordSaved,
        eventRecordUpdated,
        eventRecordDeleted,
        eventRecordForceDeleted,
        eventRecordRestored,
    }

}
