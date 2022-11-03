package swcnoops.server.commands.player;

import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.StringListResult;
import swcnoops.server.json.JsonParser;

public class PlayerHolonetGetCommandCenterEntry extends AbstractCommandAction<PlayerHolonetGetCommandCenterEntry, StringListResult> {
    @Override
    protected StringListResult execute(PlayerHolonetGetCommandCenterEntry arguments, long time) throws Exception {
        StringListResult result = new StringListResult();
        // TODO - look at which holonet the player needs to show based on time and faction
        result.getData().add("hn_cc_1_undead_raid_r");
        return result;
    }

    @Override
    protected PlayerHolonetGetCommandCenterEntry parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerHolonetGetCommandCenterEntry.class);
    }

    @Override
    public String getAction() {
        return "player.holonet.getCommandCenterEntry";
    }
}
