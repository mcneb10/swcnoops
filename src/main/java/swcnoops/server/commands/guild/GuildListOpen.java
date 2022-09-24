package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.guild.response.GuildListOpenResult;
import swcnoops.server.datasource.SelfDonatingSquad;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.Squad;
import swcnoops.server.session.PlayerSession;
import java.util.List;

public class GuildListOpen extends AbstractCommandAction<GuildListOpen, GuildListOpenResult> {

    @Override
    protected GuildListOpenResult execute(GuildListOpen arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        GuildListOpenResult guildListOpenResult = new GuildListOpenResult();
        Squad squad = createSelfDonateSquad(playerSession);
        guildListOpenResult.addSquad(squad);
        List<Squad> squads = ServiceFactory.instance().getPlayerDatasource().getGuildList(playerSession.getFaction());

        if (squads != null)
            guildListOpenResult.getSquadData().addAll(squads);

        return guildListOpenResult;
    }

    private Squad createSelfDonateSquad(PlayerSession playerSession) {
        SelfDonatingSquad selfDonatingSquad = new SelfDonatingSquad(playerSession);
        Squad squad = selfDonatingSquad.getSquad();
        return squad;
    }

    @Override
    protected GuildListOpen parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildListOpen.class);
    }

    @Override
    public String getAction() {
        return "guild.list.open";
    }
}
