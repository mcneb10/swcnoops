package swcnoops.server.datasource;

import java.util.Date;

public class SquadWar extends War {
    public long warMatchedTime;
    public Date warMatchedDate;
    private WarSignUp squadAWarSignUp;
    private WarSignUp squadBWarSignUp;

    public void setSquadAWarSignUp(WarSignUp squadAWarSignUp) {
        this.squadAWarSignUp = squadAWarSignUp;
    }

    public WarSignUp getSquadAWarSignUp() {
        return squadAWarSignUp;
    }

    public void setSquadBWarSignUp(WarSignUp squadBWarSignUp) {
        this.squadBWarSignUp = squadBWarSignUp;
    }

    public WarSignUp getSquadBWarSignUp() {
        return squadBWarSignUp;
    }
}
