package swcnoops.server.session.creature;

import swcnoops.server.datasource.CreatureSettings;

public interface CreatureManager {
    void recaptureCreature(String creatureTroopUid, long time);

    boolean hasCreature();

    String getBuildingKey();

    String getBuildingUid();

    long getRecaptureEndTime();

    boolean isCreatureAlive();

    boolean isRecapturing();

    String getCreatureUid();

    String getSpecialAttackUid();

    void creatureBuyout();

    CreatureStatus getCreatureStatus();

    CreatureSettings getCreatureSettings();
}
