package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.guild.response.SquadResult;
import swcnoops.server.json.JsonParser;

public class GuildGetPublic extends AbstractCommandAction<GuildGetPublic, SquadResult> {
    private String guildId;

    @Override
    protected SquadResult execute(GuildGetPublic arguments, long time) throws Exception {
        SquadResult squadResult = new SquadResult();
        squadResult.id = arguments.getGuildId();
        squadResult.name = "SelfDonatingSquad";
        squadResult.description = "Allows donating troops to yourself";
        squadResult.faction = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId()).getPlayerSettings().getFaction();
        return squadResult;
    }

    @Override
    protected GuildGetPublic parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildGetPublic.class);
    }

    @Override
    public String getAction() {
        return "guild.get.public";
    }

    public String getGuildId() {
        return guildId;
    }
}


