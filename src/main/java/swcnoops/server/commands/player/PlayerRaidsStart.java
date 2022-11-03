package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.player.response.RaidDefenseStartResult;
import swcnoops.server.json.JsonParser;

public class PlayerRaidsStart extends PlayerChecksum<PlayerRaidsStart, RaidDefenseStartResult> {
    private String raidMissionId;
    private String planetId;

    @Override
    protected RaidDefenseStartResult execute(PlayerRaidsStart arguments, long time) throws Exception {
        RaidDefenseStartResult raidDefenseStartResult = new RaidDefenseStartResult();
        raidDefenseStartResult.setBattleId(ServiceFactory.createRandomUUID());
        return raidDefenseStartResult;
    }

    @Override
    protected PlayerRaidsStart parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerRaidsStart.class);
    }

    @Override
    public String getAction() {
        return "player.raids.start";
    }

    public String getRaidMissionId() {
        return raidMissionId;
    }

    public void setRaidMissionId(String raidMissionId) {
        this.raidMissionId = raidMissionId;
    }

    public String getPlanetId() {
        return planetId;
    }

    public void setPlanetId(String planetId) {
        this.planetId = planetId;
    }
}
