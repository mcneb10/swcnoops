package swcnoops.server.datasource;

public interface DBCacheObjectRead<A> {
    A getObjectForReading();

    void initialise(A initialDBObject);

    void setDirty();
}
