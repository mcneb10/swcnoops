package swcnoops.server.datasource;

public interface DBCacheObjectSaving<A> {
    boolean needsSaving();

    void doneSaving();

    A getObjectForSaving();

    A setObjectForSaving(A object);
}
