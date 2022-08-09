package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.guild.response.SquadResult;
import swcnoops.server.datasource.GuildSettingsImpl;
import swcnoops.server.json.JsonParser;
import swcnoops.server.session.GuildSession;
import swcnoops.server.session.GuildSessionImpl;
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

        GuildSettingsImpl guildSettings = new GuildSettingsImpl(ServiceFactory.createRandomUUID());
        guildSettings.setName(arguments.getName());
        guildSettings.setDescription(arguments.getDescription());
        guildSettings.setIcon(arguments.getIcon());
        guildSettings.setFaction(playerSession.getFaction());
        guildSettings.setOpenEnrollment(arguments.getOpenEnrollment());
        guildSettings.setMinScoreAtEnrollment(arguments.getMinScoreAtEnrollment());
        GuildSession guildSession = new GuildSessionImpl(guildSettings);
        guildSession.login(playerSession);

        guildSession.createNewGuild(playerSession);

        guildSession = ServiceFactory.instance().getSessionManager()
                .getGuildSession(playerSession.getPlayerSettings(), guildSettings.getGuildId());
        guildSession.login(playerSession);
        SquadResult squadResult = GuildCommandAction.createSquadResult(guildSession);
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
