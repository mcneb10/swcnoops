package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.guild.response.SquadResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.session.GuildSession;
import swcnoops.server.session.PlayerSession;

public class GuildCreate extends AbstractCommandAction<GuildCreate, SquadResult> {
    private boolean openEnrollment;
    private int minScoreAtEnrollment;
    private String icon;
    private String name;
    private String description;

    @Override
    protected SquadResult execute(GuildCreate arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        SquadResult squadResult = new SquadResult();
        squadResult.id = ServiceFactory.createRandomUUID();
        squadResult.name = arguments.getName();
        squadResult.description = arguments.getDescription();
        squadResult.icon = arguments.getIcon();
        squadResult.minScoreAtEnrollment = arguments.getMinScoreAtEnrollment();
        squadResult.openEnrollment = arguments.getOpenEnrollment();
        squadResult.faction = playerSession.getFaction();

        ServiceFactory.instance().getPlayerDatasource().newGuild(playerSession.getPlayerId(), squadResult);
        GuildSession guildSession = ServiceFactory.instance().getSessionManager()
                .getGuildSession(playerSession.getPlayerSettings(), squadResult.id);
        guildSession.login(playerSession);
        squadResult = GuildCommandAction.createSquadResult(guildSession);
        return squadResult;
    }

    @Override
    protected GuildCreate parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildCreate.class);
    }

    @Override
    public String getAction() {
        return "guild.create";
    }

    public boolean getOpenEnrollment() {
        return openEnrollment;
    }

    public int getMinScoreAtEnrollment() {
        return minScoreAtEnrollment;
    }

    public String getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

}
