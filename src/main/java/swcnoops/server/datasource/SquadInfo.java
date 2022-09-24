package swcnoops.server.datasource;

import swcnoops.server.model.Member;
import swcnoops.server.model.Squad;

import java.util.ArrayList;
import java.util.List;

public class SquadInfo extends Squad {
    private List<Member> squadMembers = new ArrayList<>();

    public List<Member> getSquadMembers() {
        return squadMembers;
    }

    public void setSquadMembers(List<Member> members) {
        this.squadMembers = members;
    }
}
