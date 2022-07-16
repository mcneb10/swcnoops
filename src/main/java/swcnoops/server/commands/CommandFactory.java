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
    final private Map<String, CommandAction> commandMap = new HashMap<>();

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
        this.add(new PlayerMissionsMissionMap());
        this.add(new PlayerNameSet());
        this.add(new PlayerDeployableTrain());
        this.add(new PlayerDeployableCancel());
        this.add(new PlayerDeployableBuyout());
        this.add(new PlayerDeployableRemove());
        this.add(new PlayerDeployableSpend());
        this.add(new PlayerBuildingCapture());
        this.add(new PlayerBuildingBuyout());
        this.add(new PlayerDeployableUpgradeStart());
        this.add(new GuildTroopsRequest());
        this.add(new GuildTroopsDonate());

        this.add(new PlayerBuildingCancel());
        this.add(new PlayerBuildingMultimove());
        this.add(new PlayerBuildingCollect());
        this.add(new PlayerBuildingConstruct());
        this.add(new PlayerBuildingRearm());
        this.add(new PlayerBuildingUpgrade());
        this.add(new PlayerBuildingClear());
        this.add(new PlayerBuildingInstantUpgrade());
        this.add(new PlayerBuildingUpgradeAll());
        this.add(new PlayerBuildingSwap());

        this.add(new PlayerPveStart());
        this.add(new PlayerPvpBattleStart());
        this.add(new PlayerPveComplete());
        this.add(new PlayerPveCollect());
        this.add(new PlayerMissionsClaimMission());

        this.add(new PlayerFactionSet());

        this.add(new GuildWarMatchmakingStart());
        this.add(new GuildListOpen());
        this.add(new GuildGetPublic());
        this.add(new GuildJoin());
        this.add(new GuildLeave());

        this.add(new PlayerMissionsActivateMission());
        this.add(new PlayerMissionsClaimCampaign());

        this.add(new PlayerFueComplete());
        this.add(new PlayerFueSetQuest());
        this.add(new PlayerPvpGetNextTarget());
        this.add(new PlayerPvpBattleComplete());
        this.add(new PlayerEpisodesProgressGet());
        this.add(new PlayerIdentityGet());
        this.add(new PlayerIdentitySwitch());
        this.add(new PlayerStoreBuy());

        this.add(new GuildLeaderboardGetLeaders());
        this.add(new PlayerLeaderboardGetLeaders());
        this.add(new PlayerLeaderboardGetForFriends());

        this.add(new PlayerPlanetObjective());

        this.add(new PlayerStoreCrateBuy());
        this.add(new PlayerCrateAward());

        // TODO - this command looks like it will bring up some sort of dialog on the client related to CommandCenterVO
        this.add(new OkListCommandAction("player.holonet.getCommandCenterEntry"));

        // TODO - this command looks like it will give an update on war etc.. related to TransmissionVO
        this.add(new OkListCommandAction("player.holonet.getEventMessage"));

        // TODO - squad messages and notifications from a point in time
        this.add(new OkListCommandAction("guild.notifications.get"));

        // TODO - planet population stats maybe
        this.add(new OkCommandAction("player.planet.stats"));

//        this.add(new OkCommandAction("player.missions.showIntro"));
//        this.add(new OkCommandAction("player.missions.startSpecop"));
//        this.add(new OkCommandAction("player.building.move"));

        this.add(new OkCommandAction("player.building.collect.all"));
        this.add(new OkCommandAction("player.planet.relocate"));
        this.add(new OkCommandAction("player.raids.update"));
        this.add(new OkCommandAction("player.pvp.releaseTarget"));

        // works but needs full support
        this.add(new OkCommandAction("player.perks.activate"));
    }

    private void add(CommandAction commandAction) {
        commandMap.put(commandAction.getAction(), commandAction);
    }

    public CommandAction get(String action) {
        return this.commandMap.get(action);
    }
}
