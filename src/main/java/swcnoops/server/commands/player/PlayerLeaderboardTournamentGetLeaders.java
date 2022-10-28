package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.LeaderboardResult;
import swcnoops.server.datasource.TournamentLeaderBoard;
import swcnoops.server.datasource.TournamentStat;
import swcnoops.server.datasource.buffers.RingBuffer;
import swcnoops.server.game.ConflictManager;
import swcnoops.server.game.TournamentData;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.*;
import swcnoops.server.model.mini.MiniPlayerModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerLeaderboardTournamentGetLeaders extends AbstractCommandAction<PlayerLeaderboardTournamentGetLeaders, LeaderboardResult> {
    private String planetUid;

    @Override
    protected LeaderboardResult execute(PlayerLeaderboardTournamentGetLeaders arguments, long time) throws Exception {
        LeaderboardResult leaderboardResult = new LeaderboardResult();

        TournamentData tournamentData =
                ServiceFactory.instance().getGameDataManager().getConflictManager().getConflict(arguments.getPlanetUid());

        if (tournamentData != null) {
            TournamentLeaderBoard leaderBoard = ServiceFactory.instance().getPlayerDatasource()
                    .getTournamentLeaderBoard(tournamentData.getUid(), arguments.getPlayerId());

            ConflictManager conflictManager = ServiceFactory.instance().getGameDataManager().getConflictManager();
            conflictManager.calculatePercentile(leaderBoard);

            leaderboardResult.setLeaders(new ArrayList<>());
            leaderboardResult.setSurrounding(new ArrayList<>());

            map(leaderboardResult.getLeaders(), leaderBoard.getTop50());
            map(leaderboardResult.getSurrounding(), leaderBoard.getSurroundingMe());
        }

        return leaderboardResult;
    }

    private void map(List<PlayerEntity> leaders, RingBuffer top50) {
        Iterator<TournamentStat> iterator = top50.iterator();

        while (iterator.hasNext()) {
            TournamentStat tournamentStat = iterator.next();

            PlayerEntity playerEntity = new PlayerEntity();
            playerEntity._id = tournamentStat.playerId;
            playerEntity.rank = tournamentStat.rank;
            playerEntity.value = tournamentStat.value;
            playerEntity.account = new PlayerAccount();
            playerEntity.account.manimal = new PlayerManimal();
            playerEntity.account.manimal.data = new PlayerData();
            playerEntity.account.manimal.data.name = tournamentStat.name;
            playerEntity.account.manimal.data.playerModel = new MiniPlayerModel();
            if (tournamentStat.guildId != null) {
                playerEntity.account.manimal.data.playerModel.guildInfo = new GuildInfo();
                playerEntity.account.manimal.data.playerModel.guildInfo.guildName = tournamentStat.guildName;
                playerEntity.account.manimal.data.playerModel.guildInfo.icon = tournamentStat.icon;
            }
            playerEntity.account.manimal.data.playerModel.faction = tournamentStat.faction;
            playerEntity.account.manimal.data.playerModel.map = new PlayerMap();
            playerEntity.account.manimal.data.playerModel.map.planet = tournamentStat.planet;
//            playerEntity.account.manimal.data.scalars = new LeaderboardBattleHistory();
//            playerEntity.account.manimal.data.scalars.attacksWon = 1;
//            playerEntity.account.manimal.data.scalars.defensesWon = 2;
            playerEntity.account.manimal.data.playerModel.tournaments = new ConcurrentHashMap<>();
            LeaderboardBattleHistory leaderboardBattleHistory = new LeaderboardBattleHistory();
            leaderboardBattleHistory.attacksWon = tournamentStat.attacksWon;
            leaderboardBattleHistory.defensesWon = tournamentStat.defensesWon;
            playerEntity.account.manimal.data.playerModel.tournaments.put(tournamentStat.uid, leaderboardBattleHistory);
            leaders.add(playerEntity);
        }
    }

    @Override
    protected PlayerLeaderboardTournamentGetLeaders parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerLeaderboardTournamentGetLeaders.class);
    }

    @Override
    public String getAction() {
        return "player.leaderboard.tournament.getLeaders";
    }

    public String getPlanetUid() {
        return planetUid;
    }

    public void setPlanetUid(String planetUid) {
        this.planetUid = planetUid;
    }
}
