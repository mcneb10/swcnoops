package swcnoops.server.commands;

public class PlayerIdArguments implements CommandArguments {
    private String playerId;

    @Override
    public String getPlayerId() {
        return playerId;
    }
}
