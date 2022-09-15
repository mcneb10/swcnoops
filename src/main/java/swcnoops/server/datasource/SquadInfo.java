package swcnoops.server.datasource;

import swcnoops.server.model.Member;
import swcnoops.server.model.Squad;
import swcnoops.server.model.WarHistory;

import java.util.ArrayList;
import java.util.List;

public class SquadInfo extends Squad {
    public Long warSignUpTime;
    public String warId;
    private String description;
    private List<Member> squadMembers = new ArrayList<>();
    private List<WarHistory> warHistory = new ArrayList<>();

    public List<Member> getSquadMembers() {
        return squadMembers;
    }

    public void setSquadMembers(List<Member> members) {
        this.squadMembers = members;
    }

    public List<WarHistory> getWarHistory() {
        return warHistory;
    }

    public void setWarHistory(List<WarHistory> warHistory) {
        this.warHistory = warHistory;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
