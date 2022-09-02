package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.PlayerSession;

public class PlayerPlanetRelocate extends PlayerChecksum<PlayerPlanetRelocate, CommandResult> {
    private String planet;
    private boolean payWithHardCurrency;

    @Override
    protected CommandResult execute(PlayerPlanetRelocate arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());
        playerSession.planetRelocate(arguments.getPlanet(),
                arguments.getPayWithHardCurrency(),
                arguments.getCrystals(),
                time);

        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected PlayerPlanetRelocate parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerPlanetRelocate.class);
    }

    @Override
    public String getAction() {
        return "player.planet.relocate";
    }

    public String getPlanet() {
        return planet;
    }

    public boolean getPayWithHardCurrency() {
        return payWithHardCurrency;
    }
}
