package swcnoops.server.commands.guild.response;

import swcnoops.server.model.Squad;
import swcnoops.server.requests.AbstractCommandResult;

import java.util.ArrayList;
import java.util.List;

public class GuildListOpenResult extends AbstractCommandResult {
    private List<Squad> squadData = new ArrayList<>();

    public GuildListOpenResult() {
    }

    public void addSquad(Squad squad) {
        squadData.add(squad);
    }

    public List<Squad> getSquadData() {
        return squadData;
    }

    @Override
    public Object getResult() {
        return squadData;
    }
}
