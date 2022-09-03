package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.PlayerSession;

public class PlayerMissionsClaimCampaign extends PlayerChecksum<PlayerMissionsClaimCampaign, CommandResult> {
    private String campaignUid;
    private String missionUid;

    @Override
    protected CommandResult execute(PlayerMissionsClaimCampaign arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        playerSession.claimCampaign(arguments.getCampaignUid(), arguments.getMissionUid(), time);

        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected PlayerMissionsClaimCampaign parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerMissionsClaimCampaign.class);
    }

    @Override
    public String getAction() {
        return "player.missions.claimCampaign";
    }

    public String getCampaignUid() {
        return campaignUid;
    }

    public String getMissionUid() {
        return missionUid;
    }

    @Override
    protected boolean acceptCredits() {
        return true;
    }

    @Override
    protected boolean acceptMaterials() {
        return true;
    }

    @Override
    protected boolean acceptContraband() {
        return true;
    }

    @Override
    protected boolean acceptCrystals() {
        return true;
    }
}
