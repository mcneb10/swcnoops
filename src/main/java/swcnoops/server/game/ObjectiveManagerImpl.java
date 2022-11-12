package swcnoops.server.game;

import swcnoops.server.ServiceFactory;
import swcnoops.server.model.*;

import java.util.*;

public class ObjectiveManagerImpl implements ObjectiveManager {
    final private Random random = new Random();
    private Map<String, ObjSeriesData> planetSeries;
    private Map<String, FactionObjectives> objectiveBuckets = new HashMap<>();

    @Override
    public void setup(Collection<ObjSeriesData> values, Map<String, ObjTableData> map) {
        this.planetSeries = getActiveObjSeries(values);
        this.objectiveBuckets = getObjectiveMap(map.values());
    }

    @Override
    public Map<String, ObjectiveGroup> getObjectiveGroups(UnlockedPlanets unlockedPlanets, FactionType faction, int hqLevel) {
        if (unlockedPlanets == null) {
            unlockedPlanets = new UnlockedPlanets();
        }

        if (!unlockedPlanets.contains("planet1")) {
            unlockedPlanets.add("planet1");
        }

        List<ObjectiveGroup> objectiveGroups = new ArrayList<>(unlockedPlanets.size());
        for (String planetId : unlockedPlanets) {
            objectiveGroups.add(getObjectiveGroup(planetId, faction, hqLevel));
        }

        Map<String, ObjectiveGroup> groupMap = new HashMap<>();
        objectiveGroups.forEach(g -> groupMap.put(g.getPlanetId(), g));

        return groupMap;
    }

    @Override
    public ObjectiveGroup getObjectiveGroup(String planetId, FactionType faction, int hqLevel) {
        ObjSeriesData planetData = this.planetSeries.get(planetId);
        ObjectiveGroup objectiveGroup = createObjectiveGroup(planetData);
        ObjectiveProgress objectiveProgress1 = createObjectiveProgress(faction, planetData.getObjBucket(), hqLevel);
        objectiveProgress1.planetId = planetId;
        objectiveGroup.progress.add(objectiveProgress1);
        ObjectiveProgress objectiveProgress2 = createObjectiveProgress(faction, planetData.getObjBucket2(), hqLevel);
        objectiveProgress2.planetId = planetId;
        objectiveGroup.progress.add(objectiveProgress2);
        ObjectiveProgress objectiveProgress3 = createObjectiveProgress(faction, planetData.getObjBucket3(), hqLevel);
        objectiveProgress3.planetId = planetId;
        objectiveGroup.progress.add(objectiveProgress3);

        return objectiveGroup;
    }

    private ObjectiveProgress createObjectiveProgress(FactionType faction, String objBucket, int hqLevel) {
        FactionObjectives factionObjectives = this.objectiveBuckets.get(objBucket);
        List<ObjTableData> objectives = factionObjectives.getFactionObj(faction);

        ObjTableData objTableData;
        do {
            objTableData = objectives.get(this.random.nextInt(objectives.size()));
            if (hqLevel < objTableData.getMinHQ())
                objTableData = null;

        } while(objTableData == null);

        ObjectiveProgress objectiveProgress = new ObjectiveProgress();
        objectiveProgress.uid = objTableData.getUid();
        objectiveProgress.claimAttempt = false;
        objectiveProgress.count = 0;
        objectiveProgress.hq = hqLevel;
        objectiveProgress.target = getObjectiveTarget(objTableData, hqLevel);
        objectiveProgress.state = ObjectiveState.active;

        return objectiveProgress;
    }

    private int getObjectiveTarget(ObjTableData objTableData, int hqLevel) {
        int target = 0;
        switch (hqLevel) {
            case 4 :
                target = objTableData.getHq4();
                break;
            case 5 :
                target = objTableData.getHq5();
                break;
            case 6 :
                target = objTableData.getHq6();
                break;
            case 7 :
                target = objTableData.getHq7();
                break;
            case 8 :
                target = objTableData.getHq8();
                break;
            case 9 :
                target = objTableData.getHq9();
                break;
            case 10 :
                target = objTableData.getHq10();
                break;
            case 11 :
                target = objTableData.getHq11();
                break;
        }
        return target;
    }

    private ObjectiveGroup createObjectiveGroup(ObjSeriesData planetData) {
        // TODO - generate an index number for player, and determine start and end for player obj refresh
        ObjectiveGroup objectiveGroup = new ObjectiveGroup(planetData.getUid() + "_1", planetData.getPlanetUid());
        objectiveGroup.startTime = ServiceFactory.getSystemTimeSecondsFromEpoch() - 10;
        objectiveGroup.endTime = objectiveGroup.startTime + (60 * 60 * planetData.getPeriodHours());
        objectiveGroup.graceTime = objectiveGroup.endTime;
        return objectiveGroup;
    }

    private Map<String, FactionObjectives> getObjectiveMap(Collection<ObjTableData> values) {
        Map<String, FactionObjectives> bucketObjectives = new HashMap<>();

        for (ObjTableData data : values) {
            FactionObjectives factionObjectives = bucketObjectives.get(data.getObjBucket());
            if (factionObjectives == null) {
                factionObjectives = new FactionObjectives(data.getObjBucket());
                bucketObjectives.put(factionObjectives.getBucket(), factionObjectives);
            }

            factionObjectives.add(data);
        }

        return bucketObjectives;
    }

    private Map<String, ObjSeriesData> getActiveObjSeries(Collection<ObjSeriesData> values) {
        Map<String, ObjSeriesData> map = new HashMap<>();
        long time = ServiceFactory.getSystemTimeSecondsFromEpoch();
        for (ObjSeriesData data : values) {
            if (isActive(data, time)) {
                map.put(data.getPlanetUid(), data);
            }
        }

        return map;
    }

    private boolean isActive(ObjSeriesData data, long time) {
        if (data.getStartTime() <= time && data.getEndTime() > time)
            return true;

        return false;
    }

    private class FactionObjectives {
        private String bucket;
        private List<ObjTableData> rebelObjectives = new ArrayList<>();
        private List<ObjTableData> empireObjectives = new ArrayList<>();

        public FactionObjectives(String bucket) {
            this.bucket = bucket;
        }

        public String getBucket() {
            return bucket;
        }

        public void add(ObjTableData data) {
            List<ObjTableData> factionObjs = getFactionObj(data.getFaction());
            if (factionObjs != null)
                factionObjs.add(data);
        }

        private List<ObjTableData> getFactionObj(FactionType faction) {
            if (faction == FactionType.empire)
                return this.empireObjectives;
            else if (faction == FactionType.rebel)
                return this.rebelObjectives;

            return null;
        }
    }
}
