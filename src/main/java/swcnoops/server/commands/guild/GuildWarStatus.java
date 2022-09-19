package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.guild.response.GuildWarStatusCommandResult;
import swcnoops.server.datasource.War;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.BuffBase;
import swcnoops.server.model.Participant;
import swcnoops.server.model.SquadMemberWarData;
import swcnoops.server.model.WarSquad;
import swcnoops.server.session.GuildSession;
import swcnoops.server.session.PlayerSession;
import swcnoops.server.session.WarSession;

import java.util.ArrayList;
import java.util.List;

public class GuildWarStatus extends AbstractCommandAction<GuildWarStatus, GuildWarStatusCommandResult> {

    @Override
    protected GuildWarStatusCommandResult execute(GuildWarStatus arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());
        GuildSession guildSession = playerSession.getGuildSession();

        // TODO - probably need to change this to go through a session and use that to trigger the different stages
        // of war for processing
//        WarSession warSession = ServiceFactory.instance().getSessionManager()
//                .getWarSession(guildSession.getGuildSettings().getWarId());

        War war = guildSession.getCurrentWar();

        GuildSession squad1 = ServiceFactory.instance().getSessionManager().getGuildSession(war.getSquadIdA());
        List<SquadMemberWarData> warParticipants1 = squad1.getWarParticipants(playerSession);

        GuildSession squad2 = ServiceFactory.instance().getSessionManager().getGuildSession(war.getSquadIdB());
        List<SquadMemberWarData> warParticipants2 = squad2.getWarParticipants(playerSession);

        GuildWarStatusCommandResult guildWarStatusResponse =
                new GuildWarStatusCommandResult(guildSession);
        map(guildWarStatusResponse, war, warParticipants1, warParticipants2);
        patchWarParticipants(guildWarStatusResponse);

        // buff bases can not be null otherwise client crashes
        guildWarStatusResponse.buffBases = new ArrayList<>();
        guildWarStatusResponse.buffBases.add(create("warBuff2"));
        guildWarStatusResponse.buffBases.add(create("warBuff5"));
        guildWarStatusResponse.buffBases.add(create("warBuff6"));

        // if war has finished then must set this to true otherwise squad can not start war
        if (ServiceFactory.getSystemTimeSecondsFromEpoch() >= war.getActionEndTime())
            guildWarStatusResponse.rewardsProcessed = true;
        else
            guildWarStatusResponse.rewardsProcessed = false;

        guildWarStatusResponse.actionsStarted = false;
        return guildWarStatusResponse;
    }

    private void patchWarParticipants(GuildWarStatusCommandResult guildWarStatusResponse) {
        patchParticipantsWithBots(guildWarStatusResponse.guild);
        patchParticipantsWithBots(guildWarStatusResponse.rival);
    }

    private void patchParticipantsWithBots(WarSquad guild) {
        if (guild.participants.size() != 15) {
            for (int i = guild.participants.size(); i < 15; i++) {
                guild.participants.add(createBotParticipant(guild.guildId, i));
            }
        }
    }

    private Participant createBotParticipant(String guildId, int i) {
        Participant participant = new Participant();
        participant.score = 0;
        participant.level = 3;
        participant.id = guildId + "-BOT" + i;
        participant.name = "BOT" + i;
        participant.victoryPoints = 0;
        participant.turns = 0;
        return participant;
    }

    private BuffBase create(String warBuff) {
        BuffBase buffBase = new BuffBase();
        buffBase.battleInProgress = false;
        buffBase.buffUid = warBuff;
        buffBase.currentlyDefending = null;
        buffBase.ownerId = null;
        buffBase.level = 1;
        return buffBase;
    }

    @Override
    protected GuildWarStatus parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildWarStatus.class);
    }

    @Override
    public String getAction() {
        return "guild.war.status";
    }

    private void map(GuildWarStatusCommandResult result, War war, List<SquadMemberWarData> warParticipants1,
                     List<SquadMemberWarData> warParticipants2)
    {
        GuildSession guildSession = result.getGuildSession();
        if (guildSession != null) {
            if (war != null) {
                result.id = war.getWarId();
                result.prepGraceStartTime = war.getPrepGraceStartTime();
                result.prepEndTime = war.getPrepEndTime();
                result.actionGraceStartTime = war.getActionGraceStartTime();
                result.actionEndTime = war.getActionEndTime();
                result.cooldownEndTime = war.getCooldownEndTime();

                GuildSession guildSession1 = ServiceFactory.instance().getSessionManager()
                        .getGuildSession(war.getSquadIdA());

                result.guild = WarSquad.map(guildSession1, warParticipants1);

                GuildSession guildSession2 = ServiceFactory.instance().getSessionManager()
                        .getGuildSession(war.getSquadIdB());
                result.rival = WarSquad.map(guildSession2, warParticipants2);

                // we want the player's guild to be on the left of the war screen
                if (!guildSession.getGuildId().equals(result.guild.guildId)) {
                    WarSquad temp = result.guild;
                    result.guild = result.rival;
                    result.rival = temp;
                }
            }
        }
    }
}
