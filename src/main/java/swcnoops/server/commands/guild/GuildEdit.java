package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.PlayerSession;

public class GuildEdit extends AbstractCommandAction<GuildEdit, CommandResult> {
    public String playerId;
    public boolean openEnrollment;
    public String icon;
    public Integer minScoreAtEnrollment;
    public String description;

    @Override
    protected CommandResult execute(GuildEdit arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        if (playerSession.getGuildSession().canEdit()) {
            playerSession.getGuildSession().editGuild(arguments.getDescription(),
                    arguments.getIcon(),
                    arguments.getMinScoreAtEnrollment(),
                    arguments.getOpenEnrollment());
        }

        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected GuildEdit parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildEdit.class);
    }

    @Override
    public String getAction() {
        return "guild.edit";
    }

    @Override
    public String getPlayerId() {
        return playerId;
    }

    public boolean getOpenEnrollment() {
        return openEnrollment;
    }

    public String getIcon() {
        return icon;
    }

    public Integer getMinScoreAtEnrollment() {
        return minScoreAtEnrollment;
    }

    public String getDescription() {
        return description;
    }
}
