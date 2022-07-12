package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.json.JsonParser;
import swcnoops.server.commands.guild.response.SquadResult;
import swcnoops.server.model.Member;
import swcnoops.server.session.GuildSession;
import swcnoops.server.session.PlayerSession;

import java.util.ArrayList;

public class GuildGet extends AbstractCommandAction<GuildGet, SquadResult> {

    @Override
    protected SquadResult execute(GuildGet arguments, long time) throws Exception {
        SquadResult guildGetResult =
                parseJsonFile(ServiceFactory.instance().getConfig().guildGetTemplate, SquadResult.class);

        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        // TODO - get guild for the player
        String guildId = guildGetResult.id;
        GuildSession guildSession = ServiceFactory.instance().getSessionManager().getGuildSession(arguments.getPlayerId(), guildId);
        if (guildSession == null)
            throw new RuntimeException("Unknown guild " + guildId);

        guildSession.join(playerSession);
        mapToResponse(guildGetResult, playerSession);

        guildGetResult.warSignUpTime = ServiceFactory.getSystemTimeSecondsFromEpoch();

        for (int i = 0; i < 15; i++) {
            Member member = guildGetResult.members.get(1);
            Member newMember = new Member();
            newMember.name = Integer.valueOf(i).toString();
            newMember.playerId = ServiceFactory.createRandomUUID();
            newMember.hqLevel = 3;
            newMember.isOfficer = false;
            newMember.isOwner = false;
            newMember.planet = member.planet;
            newMember.hasPlanetaryCommand = true;
            newMember.joinDate = member.joinDate;
            newMember.warParty = 1;
            guildGetResult.members.add(newMember);
        }

        return guildGetResult;
    }

    private void mapToResponse(SquadResult guildGetResult, PlayerSession playerSession) {
        guildGetResult.warSignUpTime = null;
        guildGetResult.warRating = null;
        guildGetResult.warHistory = new ArrayList<>();
        guildGetResult.lastPerkNotif = 0;
    }

    @Override
    protected GuildGet parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildGet.class);
    }

    @Override
    public String getAction() {
        return "guild.get";
    }
}
