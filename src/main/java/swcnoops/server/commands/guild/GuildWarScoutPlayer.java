package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.guild.response.GuildWarGetParticipantResult;
import swcnoops.server.game.MapHelper;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.SquadMemberWarData;
import swcnoops.server.session.PlayerSession;

public class GuildWarScoutPlayer extends AbstractCommandAction<GuildWarScoutPlayer, GuildWarGetParticipantResult> {
    private String participantId;

    @Override
    protected GuildWarGetParticipantResult execute(GuildWarScoutPlayer arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager().getPlayerSession(arguments.getParticipantId());
        SquadMemberWarData squadMemberWarData = playerSession.getSquadMemberWarData(time);
        squadMemberWarData.champions = MapHelper.mapChampions(squadMemberWarData.warMap);
        squadMemberWarData.creatureTraps = MapHelper.mapCreatureTraps(playerSession);
        GuildWarGetParticipantResult result = new GuildWarGetParticipantResult(squadMemberWarData);
        return result;
    }

    @Override
    protected GuildWarScoutPlayer parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildWarScoutPlayer.class);
    }

    @Override
    public String getAction() {
        return "guild.war.scoutPlayer";
    }

    public String getParticipantId() {
        return participantId;
    }
}
