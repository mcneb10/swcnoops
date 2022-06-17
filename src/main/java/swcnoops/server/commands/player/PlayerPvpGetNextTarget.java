package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.PlayerPvpGetNextTargetCommandResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.Buildings;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlayerPvpGetNextTarget extends AbstractCommandAction<PlayerPvpGetNextTarget, PlayerPvpGetNextTargetCommandResult>
{
    private List<File> layouts;
    private Random rand = new Random();

    @Override
    protected PlayerPvpGetNextTargetCommandResult execute(PlayerPvpGetNextTarget arguments) throws Exception {
        PlayerPvpGetNextTargetCommandResult response =
                parseJsonFile("templates/playerPvpGetNextTarget.json", PlayerPvpGetNextTargetCommandResult.class);
        response.battleId = ServiceFactory.createRandomUUID();
        response.map.buildings = getNextLayout();
        return response;
    }

    private Buildings getNextLayout() {
        Buildings mapObject = null;
        File layoutFile = null;
        try {
            if (layouts == null) {
                layouts = listf(ServiceFactory.instance().getConfig().layoutsPath);
            }

            int index = rand.nextInt(layouts.size());
            layoutFile = this.layouts.get(index - 1);
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
