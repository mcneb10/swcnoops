package swcnoops.server.commands.player;

import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.PlayerStoreShardOffersGetResult;
import swcnoops.server.json.JsonParser;

public class PlayerStoreShardOffersGet extends AbstractCommandAction<PlayerStoreOffersGet, PlayerStoreShardOffersGetResult> {
    @Override
    protected PlayerStoreShardOffersGetResult execute(PlayerStoreOffersGet arguments, long time) throws Exception {
        PlayerStoreShardOffersGetResult playerStoreShardOffersGetResult = new PlayerStoreShardOffersGetResult();
        return playerStoreShardOffersGetResult;
    }

    @Override
    protected PlayerStoreOffersGet parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerStoreOffersGet.class);
    }

    @Override
    public String getAction() {
        return "player.store.shard.offers.get";
    }
}
