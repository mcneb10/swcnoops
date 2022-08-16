package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.guild.response.GuildListOpenResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.Squad;

import java.util.List;

public class GuildSearchByName extends AbstractCommandAction<GuildSearchByName, GuildListOpenResult> {
    private String searchTerm;

    @Override
    protected GuildListOpenResult execute(GuildSearchByName arguments, long time) throws Exception {
        GuildListOpenResult guildListOpenResult = new GuildListOpenResult();
        List<Squad> squads = ServiceFactory.instance().getPlayerDatasource().searchGuildByName(arguments.getSearchTerm());

        if (squads != null)
            guildListOpenResult.getSquadData().addAll(squads);

        return guildListOpenResult;
    }

    @Override
    protected GuildSearchByName parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildSearchByName.class);
    }

    @Override
    public String getAction() {
        return "guild.search.byName";
    }

    public String getSearchTerm() {
        return searchTerm;
    }
}
