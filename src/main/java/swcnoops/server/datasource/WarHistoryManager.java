package swcnoops.server.datasource;

import swcnoops.server.ServiceFactory;
import swcnoops.server.model.WarHistory;

import java.util.List;

public class WarHistoryManager {
    private long dirtyTime;
    final private String squadId;
    private List<WarHistory> warHistories;
    private long lastLoadedTime;

    public WarHistoryManager(String squadId) {
        this.squadId = squadId;
    }

    public void setDirty() {
        this.dirtyTime = ServiceFactory.getSystemTimeSecondsFromEpoch();
    }

    public List<WarHistory> getWarHistory() {
        List<WarHistory> histories = this.warHistories;

        if (histories == null || this.dirtyTime > this.lastLoadedTime) {
            synchronized (this) {
                histories = this.warHistories;
                if (histories == null || this.dirtyTime > this.lastLoadedTime) {
                    this.warHistories = getOrLoad();
                    this.lastLoadedTime = ServiceFactory.getSystemTimeSecondsFromEpoch();
                }
                histories = this.warHistories;
            }
        }

        return histories;
    }

    private List<WarHistory> getOrLoad() {
        List<WarHistory> warHistories = ServiceFactory.instance().getPlayerDatasource().loadWarHistory(this.squadId);
        return warHistories;
    }
}
