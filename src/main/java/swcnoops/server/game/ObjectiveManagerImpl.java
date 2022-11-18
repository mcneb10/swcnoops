package swcnoops.server.game;

import swcnoops.server.ServiceFactory;
import swcnoops.server.model.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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
    public Map<String, ObjectiveGroup> getObjectiveGroups(Map<String, ObjectiveGroup> existingObjectives,
                                                          UnlockedPlanets unlockedPlanets, Map<String, Long> receivedDonations,
                                                          FactionType faction,
                                                          int hqLevel, float offset)
    {
        unlockedPlanets = this.verifyPlanets(unlockedPlanets);

        List<ObjectiveGroup> removed = this.removeExpiredObjectives(existingObjectives);
        UnlockedPlanets planetObjectivesMissing = this.determineObjectivePlanets(existingObjectives, unlockedPlanets);
        if (!planetObjectivesMissing.isEmpty()) {
            Map<String, ObjectiveGroup> newObjectiveGroups = this.getObjectiveGroups(planetObjectivesMissing,
                    receivedDonations,
                    faction,
                    hqLevel, offset);

            if (!newObjectiveGroups.isEmpty()) {
                if (existingObjectives == null) {
                    existingObjectives = newObjectiveGroups;
                } else {
                    existingObjectives.putAll(newObjectiveGroups);
                }
            }
        }

        return existingObjectives;
    }

    private UnlockedPlanets determineObjectivePlanets(Map<String, ObjectiveGroup> existingObjectives, UnlockedPlanets unlockedPlanets) {
        if (existingObjectives == null || existingObjectives.isEmpty())
            return unlockedPlanets;

        UnlockedPlanets requiredPlanets = new UnlockedPlanets();
        unlockedPlanets.forEach(p -> {
            if (!existingObjectives.containsKey(p))
                requiredPlanets.add(p);
        });
        return requiredPlanets;
    }

    private List<ObjectiveGroup> removeExpiredObjectives(Map<String, ObjectiveGroup> existingObjectives) {
        List<ObjectiveGroup> removed = new ArrayList<>(existingObjectives.size());

        if (existingObjectives != null) {
            long now = ServiceFactory.getSystemTimeSecondsFromEpoch();
            for (String planet : new ArrayList<>(existingObjectives.keySet())) {
                ObjectiveGroup objectiveGroup = existingObjectives.get(planet);
                if (objectiveGroup.endTime < now) {
                    existingObjectives.remove(planet);
                    removed.add(objectiveGroup);
                }
            }
        }

        return removed;
    }

    @Override
    public Map<String, ObjectiveGroup> getObjectiveGroups(UnlockedPlanets unlockedPlanets,
                                                          Map<String, Long> receivedDonations,
                                                          FactionType faction, int hqLevel, float offset)
    {
        unlockedPlanets = this.verifyPlanets(unlockedPlanets);
        List<ObjectiveGroup> objectiveGroups = new ArrayList<>(unlockedPlanets.size());
        for (String planetId : unlockedPlanets) {
            Long receivedDonation = null;
            if (receivedDonations != null) {
                receivedDonation = receivedDonations.get(planetId);
            }

            ObjectiveGroup objectiveGroup = getObjectiveGroup(planetId, receivedDonation, faction, hqLevel, offset);
            if (objectiveGroup != null)
                objectiveGroups.add(objectiveGroup);
        }

        Map<String, ObjectiveGroup> groupMap = new HashMap<>();
        objectiveGroups.forEach(g -> groupMap.put(g.getPlanetId(), g));

        return groupMap;
    }

    private UnlockedPlanets verifyPlanets(UnlockedPlanets unlockedPlanets) {
        if (unlockedPlanets == null) {
            unlockedPlanets = new UnlockedPlanets();
        }

        if (!unlockedPlanets.contains("planet1")) {
            unlockedPlanets.add("planet1");
        }

        return unlockedPlanets;
    }

    @Override
    public ObjectiveGroup getObjectiveGroup(String planetId, Long receivedDonation, FactionType faction, int hqLevel, float offset) {
        ObjSeriesData planetData = this.planetSeries.get(planetId);
        if (planetData == null)
            return null;

        ObjectiveGroup objectiveGroup = createObjectiveGroup(planetData, offset);

        if (objectiveGroup == null)
            return null;

        ObjectiveProgress objectiveProgress1 = createObjectiveProgress(faction, planetData.getObjBucket(), hqLevel, receivedDonation);
        objectiveProgress1.planetId = planetId;
        objectiveGroup.progress.add(objectiveProgress1);
        ObjectiveProgress objectiveProgress2 = createObjectiveProgress(faction, planetData.getObjBucket2(), hqLevel, receivedDonation);
        objectiveProgress2.planetId = planetId;
        objectiveGroup.progress.add(objectiveProgress2);
        ObjectiveProgress objectiveProgress3 = createObjectiveProgress(faction, planetData.getObjBucket3(), hqLevel, receivedDonation);
        objectiveProgress3.planetId = planetId;
        objectiveGroup.progress.add(objectiveProgress3);

        return objectiveGroup;
    }

    private ObjectiveProgress createObjectiveProgress(FactionType faction, String objBucket, int hqLevel, Long receivedDonation) {
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

        if (objTableData.getType() == GoalType.ReceiveDonatedTroops) {
            if (receivedDonation == null)
                receivedDonation = Long.valueOf(0);
            objectiveProgress.receivedStartCount = receivedDonation;
        }

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

    private ObjectiveGroup createObjectiveGroup(ObjSeriesData planetData, float offset) {
        // TODO - generate an index number for player, and determine start and end for player obj refresh
        ObjectiveGroup objectiveGroup = new ObjectiveGroup(planetData.getUid() + "_1", planetData.getPlanetUid());

        // get the time as will use that for the daily objective
        String startDate = planetData.getStartDate();
        String hour = startDate.substring(0, 2);
        String minutes = startDate.substring(3,5);

        ZonedDateTime dayForStartOfDay = ZonedDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.DAYS);
        long startHour = Long.parseLong(hour);
        long startMinute = Long.parseLong(minutes);
        ZonedDateTime startOfObjective = dayForStartOfDay.plusHours(startHour)
                .plusMinutes(startMinute);
        long startTime = startOfObjective.toEpochSecond() - ((long)(offset * 60 * 60));

        objectiveGroup.startTime = startTime;
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

    static public int sum(Map<String, Integer> levelUpTroopsByUid) {
        final AtomicInteger total = new AtomicInteger(0);
        if (levelUpTroopsByUid != null) {
            levelUpTroopsByUid.values().stream().forEach(a -> total.addAndGet(a));
        }
        return total.get();
    }
}
