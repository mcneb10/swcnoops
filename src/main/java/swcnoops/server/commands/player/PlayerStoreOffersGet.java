package swcnoops.server.commands.player;

import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.PlayerStoreOffersGetCommandResult;
import swcnoops.server.json.JsonParser;

public class PlayerStoreOffersGet extends AbstractCommandAction<PlayerStoreOffersGet, PlayerStoreOffersGetCommandResult> {
    @Override
    public String getAction() {
        return "player.store.offers.get";
    }

    @Override
    protected PlayerStoreOffersGetCommandResult execute(PlayerStoreOffersGet arguments) throws Exception {
        PlayerStoreOffersGetCommandResult response = new PlayerStoreOffersGetCommandResult();
        return response;
    }

    @Override
    protected PlayerStoreOffersGet parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerStoreOffersGet.class);
    }
}
