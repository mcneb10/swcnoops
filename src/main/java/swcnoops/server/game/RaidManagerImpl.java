package swcnoops.server.game;

import swcnoops.server.ServiceFactory;
import swcnoops.server.model.FactionType;
import swcnoops.server.model.Raid;
import swcnoops.server.model.UnlockedPlanets;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class RaidManagerImpl implements RaidManager {
    private List<RaidData> raids;
    private Map<String, List<RaidData>> planetRaids = new HashMap<>();
    private Random random = new Random();

    @Override
    public <T extends JoeData> void setup(Collection<RaidData> raidDatums, Map<String, RaidMissionPoolData> map) {
        this.raids = new ArrayList<>(raidDatums);
        this.raids.sort((a,b) -> Integer.compare(a.getStartOrder(), b.getStartOrder()));

        for (RaidData raid : this.raids) {
            List<RaidData> raidsForPlanet = this.planetRaids.get(raid.getPlanetUid());
            if (raidsForPlanet == null) {
                raidsForPlanet = new ArrayList<>();
                this.planetRaids.put(raid.getPlanetUid(), raidsForPlanet);
            }

            raidsForPlanet.add(raid);
        }
    }

    @Override
    public Raid calculateRaidTimes(String planetId, float offset, FactionType faction, int hqLevel,
                                   Map<String, Long> raidLogs, long timeInSecondsUTC)
    {
        Long lastPlanetRaidTime = null;
        if (raidLogs != null) {
            lastPlanetRaidTime = raidLogs.get(planetId);
        }

        NextRaid nextRaid = this.calculateNextRaid(planetId, lastPlanetRaidTime, offset, timeInSecondsUTC);
        Raid raid = new Raid();
        raid.planetId = planetId;
        raid.raidStartTimeNoOffset = nextRaid.raidStartTimeNoOffset;
        raid.startTime = nextRaid.raidTimes[0];
        raid.endTime = nextRaid.raidTimes[1];
        raid.nextRaidStartTime = nextRaid.raidTimes[2];

        raid.raidId = nextRaid.raidData.getUid() ;
        raid.raidPoolId = getRaidPoolForHQLevel(nextRaid.raidData, faction, hqLevel);
        raid.raidMissionId = getRaidMission(raid.raidPoolId);
        return raid;
    }

    private String getRaidMission(String raidPoolId) {
        Map<String, RaidMissionPoolData> map = ServiceFactory.instance().getGameDataManager()
                .getPatchData().getMap(RaidMissionPoolData.class);

        RaidMissionPoolData raidMissionPoolData = map.get(raidPoolId);

        String missionId;
        if (raidMissionPoolData.getCampaignMissions().size() > 1) {
            int size = raidMissionPoolData.getCampaignMissions().size();
            missionId = raidMissionPoolData.getCampaignMissions().get(random.nextInt(size));
        } else {
            missionId = raidMissionPoolData.getCampaignMissions().get(0);
        }

        return missionId;
    }

    private String getRaidPoolForHQLevel(RaidData raidData, FactionType faction, int hqLevel) {
        int hqIndex = hqLevel - ServiceFactory.instance().getGameDataManager().getGameConstants().raids_hq_unlock_level;
        List<String> factionPool = null;
        if (faction == FactionType.empire)
            factionPool = raidData.getRaidMissionsEmpire();
        else if (faction == FactionType.rebel)
            factionPool = raidData.getRaidMissionsRebel();

        String poolId = null;

        if (factionPool != null)
            poolId = factionPool.get(hqIndex);

        return poolId;
    }

    private NextRaid calculateNextRaid(String planetId, Long lastPlanetRaidTime, float offset, long timeInSecondsUTC) {
        List<RaidData> availableRaids = this.planetRaids.get(planetId);
        ZonedDateTime dayForStartOfDay = ZonedDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.DAYS);
        long[] raidTimes = new long[4];
        int populated = 0;
        int i = 0;
        RaidData firstRaid = null;
        long raidStartTime = 0;

        do {
            RaidData raidData = availableRaids.get(i % availableRaids.size());
            ZonedDateTime startOfRaid = dayForStartOfDay.plusHours(raidData.getStartHour())
                    .plusMinutes(raidData.getStartMinute());

            ZonedDateTime dayForEndOfRaid = dayForStartOfDay;
            if (raidData.getEndOrder() < raidData.getStartOrder()) {
                dayForEndOfRaid = dayForEndOfRaid.plusDays(1);
            }

            ZonedDateTime endOfRaid = dayForEndOfRaid.plusHours(raidData.getEndHour())
                    .plusMinutes(raidData.getEndMinute());

            long startTime = startOfRaid.toEpochSecond() - ((long)(offset * 60 * 60));
            long endTime = endOfRaid.toEpochSecond() - ((long)(offset * 60 * 60));

            if (lastPlanetRaidTime == null || startOfRaid.toEpochSecond() > lastPlanetRaidTime) {
                if (endTime > timeInSecondsUTC) {
                    if (firstRaid == null) {
                        firstRaid = raidData;
                        raidStartTime = startOfRaid.toEpochSecond();
                    }

                    raidTimes[populated++] = startTime;
                    raidTimes[populated++] = endTime;
                }
            }

            i++;
            if (i == availableRaids.size()) {
                i = 0;
                dayForStartOfDay = dayForStartOfDay.plusDays(1);
            }
        } while (populated < raidTimes.length);

        // if player is not within an active raid period then we set its end to 0 so the client displays correct
        // next raid time.
        if (timeInSecondsUTC < raidTimes[0]) {
            raidTimes[1] = 0;
        }

        return new NextRaid(firstRaid, raidStartTime, raidTimes);
    }

    @Override
    public List<Raid> getRaids(UnlockedPlanets unlockedPlanets, Map<String, Long> raidLogs,
                               float timeZoneOffset, FactionType faction, int hqLevel, long time) {

        if (hqLevel < ServiceFactory.instance().getGameDataManager().getGameConstants().raids_hq_unlock_level)
            return null;

        List<Raid> playerRaids = new ArrayList<>();

        Raid planet1Raid = calculateRaidTimes("planet1", timeZoneOffset, faction, hqLevel, raidLogs, time);
        playerRaids.add(planet1Raid);

        if (unlockedPlanets != null) {
            for (String planetId : unlockedPlanets) {
                Raid raid = calculateRaidTimes(planetId, timeZoneOffset, faction, hqLevel, raidLogs, time);
                playerRaids.add(raid);
            }
        }

        return playerRaids;
    }

    private class NextRaid {
        private long[] raidTimes;
        private RaidData raidData;
        private long raidStartTimeNoOffset;

        public NextRaid(RaidData firstRaid, long raidStartTime, long[] raidTimes) {
            this.raidData = firstRaid;
            this.raidStartTimeNoOffset = raidStartTime;
            this.raidTimes = raidTimes;
        }
    }
}
