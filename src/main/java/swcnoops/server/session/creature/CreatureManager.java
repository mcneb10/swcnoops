package swcnoops.server.session.creature;

import swcnoops.server.datasource.Creature;
import swcnoops.server.session.commands.BuildingCommands;

public interface CreatureManager extends BuildingCommands {
    void recaptureCreature(String creatureTroopUid, long time);

    boolean hasCreature();

    String getBuildingUid();

    long getRecaptureEndTime();

    boolean isCreatureAlive();

    boolean isRecapturing();

    String getCreatureUid();

    String getSpecialAttackUid();

    CreatureStatus getCreatureStatus();

    Creature getCreature();
}
