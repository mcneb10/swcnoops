package swcnoops.server.commands.player;

import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerAccountExternalGet extends AbstractCommandAction<PlayerAccountExternalGet, CommandResult> {
    public PlayerAccountExternalGet() {
    }

    @Override
    protected CommandResult execute(PlayerAccountExternalGet arguments, long time) throws Exception {
        CommandResult res = new CommandResult() {
            @Override
            public Integer getStatus() {
                return Integer.valueOf(0);
            }

            @Override
            public Object getResult() {
                Map<String, List<String>> ret = new HashMap<>();
                List<String> rec = new ArrayList<>();
//                rec.add("booo");
//                ret.put("RECOVERY", rec);
                return ret;
            }
        };
        return res;
    }

    @Override
    protected PlayerAccountExternalGet parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerAccountExternalGet.class);
    }

    @Override
    public String getAction() {
        return "player.account.external.get";
    }

    @Override
    public boolean canAttachGuildNotifications() {
        return false;
    }
}
