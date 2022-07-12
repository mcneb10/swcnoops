package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.guild.response.SquadResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.Member;
import swcnoops.server.session.GuildSession;
import swcnoops.server.session.PlayerSession;
import swcnoops.server.session.SessionManager;

public class GuildJoin extends AbstractCommandAction<GuildJoin, SquadResult> {
    private String guildId;

    @Override
    protected SquadResult execute(GuildJoin arguments, long time) throws Exception {
        SessionManager sessionManager = ServiceFactory.instance().getSessionManager();

        PlayerSession playerSession = sessionManager.getPlayerSession(arguments.getPlayerId());
        GuildSession guildSession = sessionManager.getGuildSession(arguments.getPlayerId(), arguments.getGuildId());
        GuildSession oldSquad = playerSession.getGuildSession();
        if (oldSquad != null)
            oldSquad.leave(playerSession);
        guildSession.join(playerSession);

        SquadResult squadResult =
                parseJsonFile(ServiceFactory.instance().getConfig().guildGetTemplate, SquadResult.class);

        // TODO - get guild for the player
        squadResult.id = arguments.getGuildId();
        squadResult.name = "SelfDonating";
        squadResult.description = "Self donating troops";
        squadResult.faction = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId()).getPlayerSettings().getFaction();

        squadResult.warSignUpTime = ServiceFactory.getSystemTimeSecondsFromEpoch();

        for (int i = 0; i < 15; i++) {
            Member member = squadResult.members.get(1);
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
            squadResult.members.add(newMember);
        }

        return squadResult;
    }

    @Override
    protected GuildJoin parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildJoin.class);
    }

    @Override
    public String getAction() {
        return "guild.join";
    }

    public String getGuildId() {
        return guildId;
    }
}
