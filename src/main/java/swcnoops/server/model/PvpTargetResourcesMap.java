package swcnoops.server.model;


import java.util.HashMap;
import java.util.Map;

public class PvpTargetResourcesMap {

    private Map<CurrencyType, Integer> credits;
    private Map<CurrencyType, Integer> materials;
    private Map<CurrencyType, Integer> contraband;


    public PvpTargetResourcesMap(int credits, int materials, int contraband) {
        this.credits = new HashMap<>();
        this.materials = new HashMap<>();
        this.contraband = new HashMap<>();

        this.credits.put(CurrencyType.credits, credits);
        this.materials.put(CurrencyType.materials, materials);
        this.contraband.put(CurrencyType.contraband, contraband);
        System.out.println(" System.out.println(this.credits.get(ResourceTypes.credits));---"+this.credits.get(CurrencyType.credits));
    }
}
