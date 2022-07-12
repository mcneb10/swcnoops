package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.guild.response.SquadResult;
import swcnoops.server.datasource.PlayerSettings;
import swcnoops.server.datasource.SelfDonatingSquad;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.MembershipRestrictions;
import swcnoops.server.session.PlayerSession;

public class GuildGetPublic extends AbstractCommandAction<GuildGetPublic, SquadResult> {
    private String guildId;

    @Override
    protected SquadResult execute(GuildGetPublic arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        SquadResult squadResult = createSelfDonatingSquadResult(playerSession.getPlayerSettings());
        return squadResult;
    }

    private SquadResult createSelfDonatingSquadResult(PlayerSettings playerSettings) {
        SelfDonatingSquad selfDonatingSquad = new SelfDonatingSquad(playerSettings);
        SquadResult squadResult = new SquadResult();
        squadResult.id = selfDonatingSquad.getGuildId();
        squadResult.name = selfDonatingSquad.getGuildName();
        squadResult.description = selfDonatingSquad.getDescription();
        squadResult.faction = selfDonatingSquad.getFaction();
        squadResult.memberCount = selfDonatingSquad.getMembers().size();
        squadResult.activeMemberCount = selfDonatingSquad.getMembers().size();
        squadResult.level = 1;
        squadResult.rank = 1;
        squadResult.warSignUpTime = null;
        squadResult.isSameFactionWarAllowed = true;
        squadResult.membershipRestrictions = new MembershipRestrictions();
        squadResult.membershipRestrictions.faction = playerSettings.getFaction();
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


