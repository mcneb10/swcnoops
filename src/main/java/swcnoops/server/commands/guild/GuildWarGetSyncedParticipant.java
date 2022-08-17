package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.guild.response.GuildWarGetSyncedParticipantResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.SquadMemberWarData;
import swcnoops.server.session.PlayerSession;

public class GuildWarGetSyncedParticipant extends AbstractCommandAction<GuildWarGetSyncedParticipant, GuildWarGetSyncedParticipantResult> {

    @Override
    protected GuildWarGetSyncedParticipantResult execute(GuildWarGetSyncedParticipant arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager().getPlayerSession(arguments.getPlayerId());
        SquadMemberWarData squadMemberWarData = playerSession.getSquadMemberWarData();
        GuildWarGetSyncedParticipantResult result = new GuildWarGetSyncedParticipantResult(squadMemberWarData);
        return result;
    }

    @Override
    protected GuildWarGetSyncedParticipant parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildWarGetSyncedParticipant.class);
    }

    @Override
    public String getAction() {
        return "guild.war.getSyncedParticipant";
    }
}
