package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.guild.response.GuildListOpenResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.Squad;

public class GuildListOpen extends AbstractCommandAction<GuildListOpen, GuildListOpenResult> {

    @Override
    protected GuildListOpenResult execute(GuildListOpen arguments, long time) throws Exception {
        GuildListOpenResult guildListOpenResult = new GuildListOpenResult();
        Squad squad = new Squad();
        squad._id = arguments.getPlayerId();
        squad.name = "SelfDonating";
        squad.faction = ServiceFactory.instance().getSessionManager().getPlayerSession(arguments.getPlayerId())
                .getPlayer().getPlayerSettings().getFaction();
        squad.openEnrollment = true;
        squad.level = 1;
        squad.members = 1;
        guildListOpenResult.addSquad(squad);

        // TODO - read from DB and add the other squads
        return guildListOpenResult;
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
