package swcnoops.server.session.commands;

import swcnoops.server.session.CurrencyDelta;
import swcnoops.server.session.map.MapItem;

public interface BuildingCommands extends MapItem {
    CurrencyDelta buyout(int crystals, long time);

    CurrencyDelta cancel(long time, int credits, int materials, int contraband);
}
