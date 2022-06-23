package swcnoops.server.session;

import swcnoops.server.datasource.PlayerSettings;
import swcnoops.server.game.BuildingData;
import swcnoops.server.model.Building;

import java.util.List;

public interface ContractManager {
    void trainTroops(String buildingId, String unitTypeId, int quantity, long startTime);
    void cancelTrainTroops(String buildingId, String unitTypeId, int quantity, long time);
    void buyOutTrainTroops(String buildingId, String unitTypeId, int quantity, long time);
    void moveCompletedTroops(long clientTime);
    List<BuildContract> getAllTroopContracts();
    void addContractConstructor(Building building, BuildingData buildingData);

    TroopsTransport getTroopsTransport();

    TroopsTransport getSpecialAttackTransport();

    TroopsTransport getHeroTransport();

    TroopsTransport getChampionTransport();

    void initialise(PlayerSettings playerSettings);
}
