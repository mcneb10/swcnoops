package swcnoops.server.commands.guild;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.guild.response.GuildWarGetParticipantResult;
import swcnoops.server.game.TroopData;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.CreatureTrapData;
import swcnoops.server.model.SquadMemberWarData;
import swcnoops.server.session.PlayerSession;
import swcnoops.server.session.creature.CreatureManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuildWarScoutPlayer extends AbstractCommandAction<GuildWarScoutPlayer, GuildWarGetParticipantResult> {
    private String participantId;

    @Override
    protected GuildWarGetParticipantResult execute(GuildWarScoutPlayer arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager().getPlayerSession(arguments.getParticipantId());
        SquadMemberWarData squadMemberWarData = playerSession.getSquadMemberWarData(time);
        squadMemberWarData.champions = mapChampions(playerSession);
        squadMemberWarData.creatureTraps = mapCreatureTraps(playerSession);
        GuildWarGetParticipantResult result = new GuildWarGetParticipantResult(squadMemberWarData);
        return result;
    }


    private List<CreatureTrapData> mapCreatureTraps(PlayerSession playerSession) {
        List<CreatureTrapData> creatureTrapDatums = new ArrayList<>();

        CreatureManager creatureManager = playerSession.getCreatureManager();

        if (creatureManager != null && creatureManager.getCreatureUnitId() != null) {
            TroopData troopData = playerSession.getTroopInventory().getTroopByUnitId(creatureManager.getCreatureUnitId());

            if (troopData != null) {
                CreatureTrapData creatureTrapData = new CreatureTrapData();
                creatureTrapData.buildingId = creatureManager.getBuildingKey();
                creatureTrapData.specialAttackUid = creatureManager.getSpecialAttackUid();
                creatureTrapData.ready = true;
                creatureTrapData.championUid = troopData.getUid();
                creatureTrapDatums.add(creatureTrapData);
            }
        }

        return creatureTrapDatums;
    }

    private Map<String, Integer> mapChampions(PlayerSession playerSession) {
        Map<String, Integer> map = new HashMap<>();
        return map;
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
