package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.RaidUpdateResult;
import swcnoops.server.game.RaidManager;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.Raid;
import swcnoops.server.session.PlayerSession;

public class PlayerRaidsUpdate extends AbstractCommandAction<PlayerRaidsUpdate, RaidUpdateResult> {
    private String planetId;

    @Override
    protected RaidUpdateResult execute(PlayerRaidsUpdate arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        // for some reason the planet passed into the argument is not the planet the player is on
        // when moving planets
        RaidManager raidManager = ServiceFactory.instance().getGameDataManager().getRaidManager();
        RaidUpdateResult result = new RaidUpdateResult();
        Raid raid = raidManager.calculateRaidTimes(playerSession.getPlayerSettings().getBaseMap().planet,
                playerSession.getPlayerSettings().getTimeZoneOffset(),
                playerSession.getPlayerSettings().getFaction(),
                playerSession.getPlayerSettings().getHqLevel(),
                playerSession.getRaidLogsManager().getObjectForReading(),
                time);
        result.raid = raid;

        playerSession.setNextRaidSession(raid);
        return result;
    }

    @Override
    protected PlayerRaidsUpdate parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerRaidsUpdate.class);
    }

    @Override
    public String getAction() {
        return "player.raids.update";
    }

    public String getPlanetId() {
        return planetId;
    }

    public void setPlanetId(String planetId) {
        this.planetId = planetId;
    }
}
