package swcnoops.server.commands.player;

import swcnoops.server.Config;
import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.Command;
import swcnoops.server.game.Patch;
import swcnoops.server.json.JsonParser;
import swcnoops.server.commands.player.response.PlayerContentGetCommandResult;
import swcnoops.server.requests.EmptyMessage;
import swcnoops.server.requests.Messages;

import java.util.Arrays;
import java.util.List;

public class PlayerContentGet extends AbstractCommandAction<PlayerContentGet, PlayerContentGetCommandResult> {
    private List<String> defaultPatches = Arrays.asList("patches/arc.json",
            "patches/base.json",
            "patches/fue.json",
            "patches/reserved.json",
            "patches/cae.json",
            "patches/wts.json",
            "patches/holo.json",
            "patches/olc.json",
            "patches/war.json",
            "patches/prk.json",
            "patches/trp.json",
            "patches/epi.json");

    @Override
    final public String getAction() {
        return "player.content.get";
    }

    @Override
    protected PlayerContentGet parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerContentGet.class);
    }

    @Override
    protected PlayerContentGetCommandResult execute(PlayerContentGet arguments, long time) throws Exception {
        PlayerContentGetCommandResult response = new PlayerContentGetCommandResult();

        response.cdnRoots.add(ServiceFactory.instance().getConfig().cdnRoots);
        response.secureCdnRoots.add(ServiceFactory.instance().getConfig().cdnRoots);

        // TODO - add language based on player settings
        response.patches.add("strings/strings_en-US.json");

        response.patches.addAll(defaultPatches);

        if (ServiceFactory.instance().getGameDataManager().getPatchesAvailable() != null) {
            for (Patch patch : ServiceFactory.instance().getGameDataManager().getPatchesAvailable()) {
                response.patches.add("patches/" + patch.patchName);
            }
        }

        response.manifestVersion = Config.padManifestVersion(ServiceFactory.instance().getConfig().getManifestVersionToUse());
        response.manifest = Config.manifestFileTemplate + response.manifestVersion + ".json";

        return response;
    }

    @Override
    protected Messages createMessage(Command command, PlayerContentGetCommandResult commandResult) {
        return EmptyMessage.instance;
    }
}
