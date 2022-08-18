package swcnoops.server.commands.guild;

import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;

/**
 * TODO - Attacking buff bases
 */
public class GuildWarAttackBaseStart extends AbstractCommandAction<GuildWarAttackBaseStart, CommandResult> {
    @Override
    protected CommandResult execute(GuildWarAttackBaseStart arguments, long time) throws Exception {
        return null;
    }

    @Override
    protected GuildWarAttackBaseStart parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildWarAttackBaseStart.class);
    }

    @Override
    public String getAction() {
        return "guild.war.attackBase.start";
    }
}
