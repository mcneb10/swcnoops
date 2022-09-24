package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.guild.response.SquadResult;
import swcnoops.server.datasource.SelfDonatingSquad;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.Member;
import swcnoops.server.model.MembershipRestrictions;
import swcnoops.server.model.Squad;
import swcnoops.server.session.GuildSession;
import swcnoops.server.session.PlayerSession;

import java.util.List;

public class GuildGetPublic extends AbstractCommandAction<GuildGetPublic, SquadResult> {
    private String guildId;

    @Override
    protected SquadResult execute(GuildGetPublic arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        SquadResult squadResult;

        if (arguments.getGuildId().equals(arguments.getPlayerId()))
            squadResult = createSelfDonatingSquadResult(playerSession);
        else {
            GuildSession guildSession = ServiceFactory.instance().getSessionManager()
                    .getGuildSession(playerSession, arguments.getGuildId());

            squadResult = GuildCommandAction.createSquadResult(guildSession);;
        }

        return squadResult;
    }

    private SquadResult createSelfDonatingSquadResult(PlayerSession playerSession) {
        SelfDonatingSquad selfDonatingSquad = new SelfDonatingSquad(playerSession);
        Squad squad = selfDonatingSquad.getSquad();
        SquadResult squadResult = new SquadResult();
        squadResult.id = selfDonatingSquad.getGuildId();
        squadResult.name = squad.name;
        squadResult.description = squad.description;
        squadResult.faction = squad.faction;
        List<Member> members = selfDonatingSquad.getMembers();
        squadResult.memberCount = members.size();
        squadResult.activeMemberCount = members.size();
        squadResult.level = 1;
        squadResult.rank = 1;
        squadResult.warSignUpTime = null;
        squadResult.isSameFactionWarAllowed = true;
        squadResult.membershipRestrictions = new MembershipRestrictions();
        squadResult.membershipRestrictions.faction = playerSession.getFaction();
        squadResult.membershipRestrictions.openEnrollment = true;
        squadResult.membershipRestrictions.maxSize = 15;
        squadResult.membershipRestrictions.minScoreAtEnrollment = 0;
        return squadResult;
    }

    @Override
    protected GuildGetPublic parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildGetPublic.class);
    }

    @Override
    public String getAction() {
        return "guild.get.public";
    }

    public String getGuildId() {
        return guildId;
    }
}


