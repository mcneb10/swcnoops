package swcnoops.server.datasource;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class ReadOnlyDBCacheObject<A> implements DBCacheObjectRead<A> {
    private A dbObject;
    private Lock lock = new ReentrantLock();
    private long lastLoaded;
    private long dirtyTime;
    private boolean nullAllowed = true;

    public ReadOnlyDBCacheObject(A object, boolean nullAllowed) {
        this.dbObject = object;
        this.nullAllowed = nullAllowed;
        this.lastLoaded = System.currentTimeMillis();
    }

    @Override
    public A getObjectForReading() {
        return this.getDBObject();
    }

    protected abstract A loadDBObject();

    protected A getDBObject() {
        A ret = this.dbObject;
        if ((!this.nullAllowed && ret == null) || this.dirtyTime > this.lastLoaded) {
            this.lock.lock();
            try {
                if ((!this.nullAllowed && ret == null) || this.dirtyTime > this.lastLoaded) {
                    setDbObject(loadDBObject());
                }
                ret = this.dbObject;
            } finally {
                this.lock.unlock();
            }
        }

        return ret;
    }

    private void setDbObject(A dbObject) {
        this.dbObject = dbObject;
        this.lastLoaded = System.currentTimeMillis();
    }

    public void setDirty() {
        this.dirtyTime = System.currentTimeMillis();
    }
}
