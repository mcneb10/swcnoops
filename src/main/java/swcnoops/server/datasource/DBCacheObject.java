package swcnoops.server.datasource;

public interface DBCacheObject<A> extends DBCacheObjectSaving<A> {
    A getObjectForWriting();

    A getObjectForReading();
}
