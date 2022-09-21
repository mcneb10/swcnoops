package swcnoops.server.datasource;

public interface DBCacheObject<A> extends DBCacheObjectSaving<A>, DBCacheObjectRead<A> {
    A getObjectForWriting();
}
