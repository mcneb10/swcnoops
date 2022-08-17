package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.guild.response.GuildWarGetParticipantResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.SquadMemberWarData;
import swcnoops.server.session.PlayerSession;

public class GuildWarGetParticipant extends AbstractCommandAction<GuildWarGetParticipant, GuildWarGetParticipantResult>
{
    @Override
    public String getAction() {
        return "guild.war.getParticipant";
    }

    @Override
    protected GuildWarGetParticipantResult execute(GuildWarGetParticipant arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager().getPlayerSession(arguments.getPlayerId());
        SquadMemberWarData squadMemberWarData = playerSession.getSquadMemberWarData();
        GuildWarGetParticipantResult result = new GuildWarGetParticipantResult(squadMemberWarData);
        return result;
    }

    @Override
    protected GuildWarGetParticipant parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildWarGetParticipant.class);
    }
}
