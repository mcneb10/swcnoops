package swcnoops.server.commands;

import swcnoops.server.commands.auth.GetAuthToken;
import swcnoops.server.commands.auth.preauth.AuthPreauthGeneratePlayerWithFacebook;
import swcnoops.server.commands.auth.preauth.GeneratePlayer;
import swcnoops.server.commands.config.ConfigEndpointsGet;
import swcnoops.server.commands.guild.*;
import swcnoops.server.commands.player.*;

import java.util.HashMap;
import java.util.Map;

public class CommandFactory {
    private Map<String, CommandAction> commandMap = new HashMap<>();

    public CommandFactory() {
        this.add(new AuthPreauthGeneratePlayerWithFacebook());
        this.add(new GeneratePlayer());
        this.add(new GetAuthToken());
        this.add(new ConfigEndpointsGet());
        this.add(new PlayerLogin());
        this.add(new PlayerContentGet());
        this.add(new PlayerDeviceRegister());
        this.add(new PlayerPreferencesSet());
        this.add(new GuildInviteGet());
        this.add(new PlayerAccountExternalGet());
        this.add(new GuildGetChatKey());
        this.add(new GuildWarGetParticipant());
        this.add(new GuildGet());
        this.add(new GuildWarStatus());
        this.add(new PlayerKeepAlive());
        this.add(new PlayerLeaderboardTournamentGetRanks());
        this.add(new PlayerStoreOffersGet());
        this.add(new PlayerStoreShardOffersGet());
        this.add(new PlayerCrateCheckDaily());
        this.add(new PlayerPveStart());
        this.add(new PlayerMissionsMissionMap());

        // these are unsure at the moment and needs to be looked at again
        this.add(new OkListCommandAction("player.holonet.getCommandCenterEntry"));
        this.add(new OkListCommandAction("player.holonet.getEventMessage"));
        this.add(new OkCommandAction("player.building.multimove"));
        this.add(new OkListCommandAction("guild.notifications.get"));
        this.add(new OkCommandAction("player.planet.stats"));
        this.add(new OkCommandAction("player.missions.activateMission"));
        this.add(new OkCommandAction("player.building.collect"));
        this.add(new OkCommandAction("player.building.construct"));
        this.add(new OkCommandAction("player.building.buyout"));
        this.add(new OkCommandAction("player.pve.complete"));
        this.add(new OkCommandAction("player.store.buy"));
        this.add(new OkCommandAction("player.pve.collect"));
        this.add(new OkCommandAction("player.missions.claimCampaign"));
        this.add(new OkCommandAction("player.missions.showIntro"));
        this.add(new OkCommandAction("player.missions.startSpecop"));
        this.add(new OkCommandAction("player.missions.claimMission"));
        this.add(new OkCommandAction("player.faction.set"));
        this.add(new OkCommandAction("player.name.set"));
        this.add(new OkCommandAction("player.building.upgrade"));
        this.add(new OkCommandAction("player.building.upgradeAll"));
        this.add(new OkCommandAction("player.building.instantUpgrade"));
        this.add(new OkCommandAction("player.building.swap"));
        this.add(new OkCommandAction("player.building.rearm"));
        this.add(new OkCommandAction("player.building.multimove"));
        this.add(new OkCommandAction("player.building.move"));
        this.add(new OkCommandAction("player.building.cancel"));
        this.add(new OkCommandAction("player.building.clear"));
        this.add(new OkCommandAction("player.deployable.train"));
        this.add(new OkCommandAction("player.deployable.buyout"));
        this.add(new OkCommandAction("player.deployable.cancel"));
        this.add(new OkCommandAction("player.deployable.spend"));
        this.add(new OkCommandAction("player.deployable.upgrade.start"));
        this.add(new OkCommandAction("player.fue.setQuest"));
        this.add(new OkCommandAction("player.fue.complete"));
        this.add(new OkCommandAction("player.building.collect.all"));
        this.add(new OkCommandAction("player.planet.relocate"));
        this.add(new OkCommandAction("player.raids.update"));
        this.add(new PlayerPvpGetNextTarget());
        this.add(new OkCommandAction("player.pvp.battle.start"));
        this.add(new PlayerPvpBattleComplete());
        this.add(new PlayerEpisodesProgressGet());
        this.add(new OkCommandAction("player.pvp.releaseTarget"));
    }

    final private void add(CommandAction commandAction) {
        commandMap.put(commandAction.getAction(), commandAction);
    }

    public CommandAction get(String action) {
        return this.commandMap.get(action);
    }
}
