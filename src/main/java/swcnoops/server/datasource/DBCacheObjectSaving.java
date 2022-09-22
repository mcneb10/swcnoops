package swcnoops.server.datasource;

public interface DBCacheObjectSaving<A> {
    boolean needsSaving();

    void doneDBSave();

    A getObjectForSaving();

    A setObjectForSaving(A object);
}
