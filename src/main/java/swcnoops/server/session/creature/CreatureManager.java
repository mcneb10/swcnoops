package swcnoops.server.session.creature;

import swcnoops.server.datasource.Creature;
import swcnoops.server.session.commands.BuildingCommands;
import swcnoops.server.session.map.StrixBeacon;

public interface CreatureManager extends BuildingCommands {
    void recaptureCreature(String creatureTroopUnitId, long time);

    boolean hasCreature();

    String getBuildingUid();

    long getRecaptureEndTime();

    boolean isCreatureAlive();

    boolean isRecapturing();

    String getCreatureUnitId();

    String getSpecialAttackUid();

    CreatureStatus getCreatureStatus();

    Creature getCreature();

    void creatureTrapComplete(StrixBeacon strixBeacon, long endTime);
}
