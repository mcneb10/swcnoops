package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.DeploymentRecord;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.PlayerSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerDeployableSpend extends AbstractCommandAction<PlayerDeployableSpend, CommandResult> {
    private String battleId;
    private boolean guildTroopsSpent;
    private List<DeploymentRecord> units;

    @Override
    protected CommandResult execute(PlayerDeployableSpend arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        Map<String, Integer> troopsDeployed = map(arguments.getUnits());
        playerSession.removeDeployedTroops(troopsDeployed, time);
        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    private Map<String, Integer> map(List<DeploymentRecord> units) {
        Map<String, Integer> troopsDeployed = new HashMap<>();
        for (DeploymentRecord deploymentRecord : units) {
            Integer troopCount = troopsDeployed.get(deploymentRecord.getUid());
            if (troopCount == null) {
                troopCount = Integer.valueOf(0);
            }
            troopCount = Integer.valueOf(troopCount.intValue() + 1);
            troopsDeployed.put(deploymentRecord.getUid(), troopCount);
        }

        return troopsDeployed;
    }

    @Override
    protected PlayerDeployableSpend parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerDeployableSpend.class);
    }

    @Override
    public String getAction() {
        return "player.deployable.spend";
    }

    public String getBattleId() {
        return battleId;
    }

    public boolean isGuildTroopsSpent() {
        return guildTroopsSpent;
    }

    public List<DeploymentRecord> getUnits() {
        return units;
    }
}
