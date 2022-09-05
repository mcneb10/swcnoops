package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.CommandArguments;
import swcnoops.server.commands.OkCommandAction;
import swcnoops.server.requests.CommandResult;

public class PlayerKeepAlive extends OkCommandAction {

    public PlayerKeepAlive() {
        super("player.keepAlive");
    }


    @Override
    protected CommandResult execute(CommandArguments arguments, long time) throws Exception {
        System.out.println("KEEP ALIVE---->"+arguments.getPlayerId());
        ServiceFactory.instance().getSessionManager().getPlayerSession(arguments.getPlayerId()).getPlayerSettings().setKeepAlive(time);
        ServiceFactory.instance().getSessionManager().getPlayerSession(arguments.getPlayerId()).savePlayerSession();

        return super.execute(arguments, time);
    }
}
