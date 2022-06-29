package swcnoops.server.session.commands;

import swcnoops.server.session.buildings.MapItem;

public interface BuildingCommands extends MapItem {
    void buyout(long time);

    void cancel(long time);
}
