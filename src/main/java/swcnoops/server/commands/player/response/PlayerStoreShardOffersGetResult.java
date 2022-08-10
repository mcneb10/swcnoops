package swcnoops.server.commands.player.response;

import swcnoops.server.model.ShardShopData;
import swcnoops.server.requests.AbstractCommandResult;

// TODO - for now nothing returns to make shard shop empty
public class PlayerStoreShardOffersGetResult extends AbstractCommandResult {
    public ShardShopData shardShopData = new ShardShopData();
}
