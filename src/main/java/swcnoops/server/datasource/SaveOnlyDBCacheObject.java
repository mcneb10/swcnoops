package swcnoops.server.datasource;

public class SaveOnlyDBCacheObject<A> implements DBCacheObjectSaving<A>{
    private A object;

    @Override
    public boolean needsSaving() {
        return object != null;
    }

    @Override
    public void doneSaving() {
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
