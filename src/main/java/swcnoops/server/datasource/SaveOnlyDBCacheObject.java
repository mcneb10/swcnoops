package swcnoops.server.datasource;

public class SaveOnlyDBCacheObject<A> implements DBCacheObjectSaving<A>{
    volatile private A object;

    @Override
    public boolean needsSaving() {
        return object != null;
    }

    @Override
    public void doneDBSave() {
        this.object = null;
    }

    @Override
    public A getObjectForSaving() {
        return this.object;
    }

    @Override
    public A setObjectForSaving(A object) {
        this.object = object;
        return this.object;
    }
}
