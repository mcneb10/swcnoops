package swcnoops.server.session.commands;

import swcnoops.server.session.map.MapItem;

public interface BuildingCommands extends MapItem {
    void buyout(long time);

    void cancel(long time);
}
