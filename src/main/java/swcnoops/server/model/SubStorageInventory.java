package swcnoops.server.model;

import java.util.HashMap;
import java.util.Map;

public class SubStorageInventory {
    public long capacity;
    public Map<String, StorageAmount> storage = new HashMap<>();
    public Object subStorage;
}
