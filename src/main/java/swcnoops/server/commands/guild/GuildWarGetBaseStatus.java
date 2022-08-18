package swcnoops.server.commands.guild;

import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.guild.response.GuildWarGetBaseStatusResult;
import swcnoops.server.json.JsonParser;

public class GuildWarGetBaseStatus extends AbstractCommandAction<GuildWarGetBaseStatus, GuildWarGetBaseStatusResult> {
    private String buffBaseUid;

    @Override
    protected GuildWarGetBaseStatusResult execute(GuildWarGetBaseStatus arguments, long time) throws Exception {

        // TODO - to complete
        GuildWarGetBaseStatusResult result = new GuildWarGetBaseStatusResult();
        result.buffUid = arguments.getBuffBaseUid();
        return result;
    }

    @Override
    protected GuildWarGetBaseStatus parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildWarGetBaseStatus.class);
    }

    @Override
    public String getAction() {
        return "guild.war.getBaseStatus";
    }

    public String getBuffBaseUid() {
        return buffBaseUid;
    }
}
