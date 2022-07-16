package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.PlayerStoreCrateBuyResult;
import swcnoops.server.game.BuildingType;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.SupplyData;
import swcnoops.server.session.PlayerSession;

// TODO - needs to be completed, for now to get pass the completion of armory
public class PlayerStoreCrateBuy extends AbstractCommandAction<PlayerStoreCrateBuy, PlayerStoreCrateBuyResult> {
    private String crateId;

    @Override
    protected PlayerStoreCrateBuyResult execute(PlayerStoreCrateBuy arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        String faction = playerSession.getPlayerSettings().getFaction().getNameForLookup();
        String supplyId = "lcitem_shrd_eqpRebelPentagonJetpackTrooper_st3_normal".replace("Rebel", faction);
        PlayerStoreCrateBuyResult playerStoreCrateBuyResult = new PlayerStoreCrateBuyResult();
        SupplyData supplyData = new SupplyData(supplyId, "lcpool_shrd_st3");
        playerStoreCrateBuyResult.getCrateData().resolvedSupplies.add(supplyData);
        playerStoreCrateBuyResult.getCrateData().crateId = arguments.crateId;
        playerStoreCrateBuyResult.getCrateData().hqLevel = playerSession.getPlayerMapItems()
                .getMapItemByType(BuildingType.HQ).getBuildingData().getLevel();
        playerStoreCrateBuyResult.getCrateData().claimed = false;
        playerStoreCrateBuyResult.getCrateData().doesExpire = false;
        playerStoreCrateBuyResult.getCrateData().received = time;
        playerStoreCrateBuyResult.getCrateData().uid = ServiceFactory.createRandomUUID();

        return playerStoreCrateBuyResult;
    }

    @Override
    protected PlayerStoreCrateBuy parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerStoreCrateBuy.class);
    }

    @Override
    public String getAction() {
        return "player.store.crate.buy";
    }

    public String getCrateId() {
        return crateId;
    }
}
