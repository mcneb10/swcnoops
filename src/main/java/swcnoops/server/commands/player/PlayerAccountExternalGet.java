package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.session.PlayerSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerAccountExternalGet extends AbstractCommandAction<PlayerAccountExternalGet, CommandResult> {
    public PlayerAccountExternalGet() {
    }

    @Override
    protected CommandResult execute(PlayerAccountExternalGet arguments, long time) throws Exception {
        final PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        CommandResult res = new CommandResult() {
            @Override
            public Integer getStatus() {
                return Integer.valueOf(0);
            }

            @Override
            public Object getResult() {
                Map<String, List<String>> ret = new HashMap<>();
                List<String> rec = new ArrayList<>();

                // if they are logging where the account was missing secret then we trigger recovery process
                if (playerSession.getPlayer().getPlayerSecret().getMissingSecret()) {
                    rec.add("booo");
                    ret.put("RECOVERY", rec);
                }
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
