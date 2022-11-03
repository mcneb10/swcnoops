package swcnoops.server.commands.player;

import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;

public class PlayerStoreTournamentRedeem extends PlayerChecksum<PlayerStoreTournamentRedeem, CommandResult> {
    @Override
    protected CommandResult execute(PlayerStoreTournamentRedeem arguments, long time) throws Exception {
        // TODO - to finish
        // look at for which player, see which tournaments they have
        // which ones has finished and if they have collected
        // return back correct response of class TournamentResponse
        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected PlayerStoreTournamentRedeem parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerStoreTournamentRedeem.class);
    }

    @Override
    public String getAction() {
        return "player.store.tournament.redeem";
    }
}
