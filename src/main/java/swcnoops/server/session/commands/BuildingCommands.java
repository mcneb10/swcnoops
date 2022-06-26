package swcnoops.server.session.commands;

public interface BuildingCommands {
    /**
     * The id of the building on the map
     * @return
     */
    String getBuildingId();
    void buyout(long time);

    void cancel(long time);
}
