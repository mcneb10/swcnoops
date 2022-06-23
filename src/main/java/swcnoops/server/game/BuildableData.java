package swcnoops.server.game;

public interface BuildableData {
    long getBuildingTime();

    int getSize();

    boolean isSpecialAttack();

    String getType();
    String getContractType();
}
