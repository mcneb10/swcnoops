package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.guild.response.SquadResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.CurrencyType;
import swcnoops.server.model.Squad;
import swcnoops.server.session.CurrencyDelta;
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

        Squad squad = new Squad();
        squad._id = ServiceFactory.createRandomUUID();
        squad.name = arguments.getName();
        squad.description = arguments.getDescription();
        squad.faction = playerSession.getFaction();
        squad.activeMemberCount = 1;
        squad.members = 1;
        squad.openEnrollment = arguments.getOpenEnrollment();
        squad.icon = arguments.getIcon();
        squad.minScore = arguments.getMinScoreAtEnrollment();
        squad.created = ServiceFactory.getSystemTimeSecondsFromEpoch();

        int cost = ServiceFactory.instance().getGameDataManager().getGameConstants().squad_create_cost;
        CurrencyDelta currencyDelta = new CurrencyDelta(cost, cost, CurrencyType.credits, true);
        playerSession.processInventoryStorage(currencyDelta);
        ServiceFactory.instance().getPlayerDatasource().newGuild(playerSession, squad);

        GuildSession guildSession = ServiceFactory.instance().getSessionManager()
                .getGuildSession(playerSession, squad._id);
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
