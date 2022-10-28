package swcnoops.server.commands.player.response;

import swcnoops.server.model.PlayerEntity;
import swcnoops.server.requests.AbstractCommandResult;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardResult extends AbstractCommandResult {
    private List<PlayerEntity> leaders;
    private List<PlayerEntity> surrounding;

    public LeaderboardResult() {
        this.leaders = new ArrayList<>();
        this.surrounding = new ArrayList<>();
    }

    public List<PlayerEntity> getLeaders() {
        return leaders;
    }

    public List<PlayerEntity> getSurrounding() {
        return surrounding;
    }

    public void setLeaders(List<PlayerEntity> leaders) {
        this.leaders = leaders;
    }

    public void setSurrounding(List<PlayerEntity> surrounding) {
        this.surrounding = surrounding;
    }
}

