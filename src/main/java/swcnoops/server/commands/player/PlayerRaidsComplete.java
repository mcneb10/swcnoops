package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.player.response.RaidDefenseCompleteResult;
import swcnoops.server.game.RaidManager;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.BattleReplay;
import swcnoops.server.model.Raid;
import swcnoops.server.session.PlayerSession;

public class PlayerRaidsComplete extends PlayerBattleComplete<PlayerRaidsComplete, RaidDefenseCompleteResult> {
    private String waveId;

    @Override
    protected RaidDefenseCompleteResult execute(PlayerRaidsComplete arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        // used heroes, traps, air, dekas and creature
        BattleReplay battleReplay = BattleReplay.map(arguments, playerSession, time);
        playerSession.raidsComplete(playerSession.getNextRaidSession(), battleReplay,
                arguments.getDamagedBuildings(), time);

        RaidManager raidManager = ServiceFactory.instance().getGameDataManager().getRaidManager();
        Raid raid = raidManager.calculateRaidTimes(arguments.getPlanetId(),
                playerSession.getPlayerSettings().getTimeZoneOffset(),
                playerSession.getPlayerSettings().getFaction(),
                playerSession.getPlayerSettings().getHqLevel(),
                playerSession.getRaidLogsManager().getObjectForReading(),
                time);

        // TODO - set reward
        RaidDefenseCompleteResult raidDefenseCompleteResult = new RaidDefenseCompleteResult();
        raidDefenseCompleteResult.setAwardedCrateUid(null);
        raidDefenseCompleteResult.setRaidData(raid);
        return raidDefenseCompleteResult;
    }

    @Override
    protected PlayerRaidsComplete parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerRaidsComplete.class);
    }

    @Override
    public String getAction() {
        return "player.raids.complete";
    }

    public String getWaveId() {
        return waveId;
    }

    public void setWaveId(String waveId) {
        this.waveId = waveId;
    }
}
