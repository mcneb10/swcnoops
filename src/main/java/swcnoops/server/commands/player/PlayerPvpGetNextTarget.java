package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.PlayerPvpGetNextTargetCommandResult;
import swcnoops.server.game.BuildingData;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.Building;
import swcnoops.server.model.Buildings;
import swcnoops.server.model.CreatureTrapData;
import swcnoops.server.model.PlayerMap;
import swcnoops.server.session.creature.CreatureDataMap;
import swcnoops.server.session.creature.CreatureManagerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlayerPvpGetNextTarget extends AbstractCommandAction<PlayerPvpGetNextTarget, PlayerPvpGetNextTargetCommandResult>
{
    private List<File> layouts;
    private Random rand = new Random();

    @Override
    protected PlayerPvpGetNextTargetCommandResult execute(PlayerPvpGetNextTarget arguments, long time) throws Exception {
        PlayerPvpGetNextTargetCommandResult response =
                parseJsonFile("templates/playerPvpGetNextTarget.json", PlayerPvpGetNextTargetCommandResult.class);
        response.battleId = ServiceFactory.createRandomUUID();
        response.map.buildings = getNextLayout();
        setupDefenseTroops(response, response.map);
        return response;
    }

    private void setupDefenseTroops(PlayerPvpGetNextTargetCommandResult response, PlayerMap map) {
        response.champions.clear();
        for (Building building : map.buildings) {
            BuildingData buildingData =
                    ServiceFactory.instance().getGameDataManager().getBuildingDataByUid(building.uid);
            if (buildingData != null) {
                switch (buildingData.getType()) {
                    case champion_platform:
                        response.champions.put(buildingData.getLinkedUnit(), Integer.valueOf(1));
                        break;
                }
            }
        }

        // TODO - change to have a random creature
        CreatureDataMap creatureDataMap = CreatureManagerFactory.findCreatureTrap(map);
        if (creatureDataMap != null && creatureDataMap.building != null) {
            response.creatureTrapData = new ArrayList<>();
            CreatureTrapData creatureTrapData = new CreatureTrapData();
            creatureTrapData.ready = true;
            creatureTrapData.buildingId = creatureDataMap.building.key;
            creatureTrapData.specialAttackUid = creatureDataMap.trapData.getEventData();
            creatureTrapData.championUid = "troopRebelRancorCreature10";
            response.creatureTrapData.add(creatureTrapData);
        }
    }

    private Buildings getNextLayout() {
        Buildings mapObject = null;
        File layoutFile;
        try {
            if (layouts == null || layouts.size() == 0) {
                layouts = listf(ServiceFactory.instance().getConfig().layoutsPath);
            }

            int index = rand.nextInt(layouts.size());
            if (index < 0)
                index = 0;

            if (index >= layouts.size())
                index = layouts.size() - 1;

            layoutFile = this.layouts.get(index);
            this.layouts.remove(index);
            mapObject = ServiceFactory.instance().getJsonParser().fromJsonFile(layoutFile.getAbsolutePath(), Buildings.class);
            return mapObject;
        } catch(Exception ex) {
            // TODO
            System.out.println(ex);
        }

        return mapObject;
    }

    private List<File> listf(String directoryName) {
        File directory = new File(directoryName);
        List<File> resultList = new ArrayList<>();
        // get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isDirectory()) {
                resultList.addAll(listf(file.getAbsolutePath()));
            } else if (file.getAbsolutePath().toLowerCase().endsWith(".json")) {
                resultList.add(file);
            }
        }
        return resultList;
    }

    @Override
    protected PlayerPvpGetNextTarget parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerPvpGetNextTarget.class);
    }

    @Override
    public String getAction() {
        return "player.pvp.getNextTarget";
    }
}
