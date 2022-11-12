package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.DeploymentRecord;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.PlayerSession;

import java.util.List;

public class GuildWarDeployableSpend extends AbstractCommandAction<GuildWarDeployableSpend, CommandResult> {
    private String battleId;
    private boolean guildTroopsSpent;
    private List<DeploymentRecord> units;

    @Override
    protected CommandResult execute(GuildWarDeployableSpend arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager().getPlayerSession(arguments.getPlayerId());
        playerSession.removeSpentTroops(arguments.getUnits(), time);
        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected GuildWarDeployableSpend parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildWarDeployableSpend.class);
    }

    @Override
    public String getAction() {
        return "guild.war.deployable.spend";
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
