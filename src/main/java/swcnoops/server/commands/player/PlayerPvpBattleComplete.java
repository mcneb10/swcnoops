package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.PlayerPvpBattleCompleteCommandResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.ReplayData;
import swcnoops.server.session.PlayerSession;

import java.util.Map;

public class PlayerPvpBattleComplete extends AbstractCommandAction<PlayerPvpBattleComplete, PlayerPvpBattleCompleteCommandResult> {
    private String battleId;
    private String battleUid;
    private Map<String,Integer> attackingUnitsKilled;
    private Map<String,Integer> defenderGuildTroopsSpent;
    private Map<String,Integer> attackerGuildTroopsSpent;
    private int stars;
    private String planetId;
    private ReplayData replayData;

    @Override
    protected PlayerPvpBattleCompleteCommandResult execute(PlayerPvpBattleComplete arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        playerSession.pvpBattleComplete(arguments.attackingUnitsKilled, time);

        PlayerPvpBattleCompleteCommandResult response =
                ServiceFactory.instance().getJsonParser()
                        .toObjectFromResource("templates/playerPvpBattleComplete.json", PlayerPvpBattleCompleteCommandResult.class);

        response.attackerTournament.uid = ServiceFactory.createRandomUUID();
        return response;
    }

    @Override
    protected PlayerPvpBattleComplete parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerPvpBattleComplete.class);
    }

    @Override
    public String getAction() {
        return "player.pvp.battle.complete";
    }

    public Map<String, Integer> getAttackerGuildTroopsSpent() {
        return attackerGuildTroopsSpent;
    }

    public String getBattleId() {
        return battleId;
    }

    public String getBattleUid() {
        return battleUid;
    }

    public Map<String, Integer> getAttackingUnitsKilled() {
        return attackingUnitsKilled;
    }

    public Map<String, Integer> getDefenderGuildTroopsSpent() {
        return defenderGuildTroopsSpent;
    }

    public int getStars() {
        return stars;
    }

    public String getPlanetId() {
        return planetId;
    }

    public ReplayData getReplayData() {
        return replayData;
    }
}
