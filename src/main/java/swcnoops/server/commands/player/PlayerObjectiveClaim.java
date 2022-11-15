package swcnoops.server.commands.player;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.CrateDataResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.ObjectiveGroup;
import swcnoops.server.model.ObjectiveProgress;
import swcnoops.server.model.ObjectiveState;
import swcnoops.server.session.PlayerSession;
import java.util.Optional;

// TODO - to finish
// look at which objective is being claimed, get its reward and return
public class PlayerObjectiveClaim extends AbstractCommandAction<PlayerObjectiveClaim, CrateDataResult> {
    private static final Logger LOG = LoggerFactory.getLogger(PlayerObjectiveClaim.class);

    private String planetId;
    private String objectiveId;

    @Override
    protected CrateDataResult execute(PlayerObjectiveClaim arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        ObjectiveGroup group =
                playerSession.getPlayerObjectivesManager().getObjectForReading().get(arguments.getPlanetId());

        ObjectiveProgress progress = getProgress(group, arguments.getObjectiveId());
        if (progress != null && !progress.claimAttempt) {
            if (progress.state != ObjectiveState.complete) {
                LOG.warn("Player is claiming an incomplete objective, which has " + progress.count + " for target "
                        + progress.target + " for player " + arguments.getPlayerId());
            }

            progress.claimAttempt = true;
            progress.state = ObjectiveState.rewarded;
            playerSession.getPlayerObjectivesManager().getObjectForWriting();
            playerSession.savePlayerKeepAlive();
        }

        // TODO - do the reward
        return new CrateDataResult();
    }

    private ObjectiveProgress getProgress(ObjectiveGroup group, String objectiveId) {
        ObjectiveProgress objectiveProgress = null;
        if (group.progress != null) {
            Optional<ObjectiveProgress> o = group.progress.stream().filter(p -> p.uid.equals(objectiveId)).findFirst();
            if (o.isPresent()) {
                objectiveProgress = o.get();
            }
        }

        return objectiveProgress;
    }

    @Override
    protected PlayerObjectiveClaim parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerObjectiveClaim.class);
    }

    @Override
    public String getAction() {
        return "player.objective.claim";
    }

    public String getPlanetId() {
        return planetId;
    }

    public void setPlanetId(String planetId) {
        this.planetId = planetId;
    }

    public String getObjectiveId() {
        return objectiveId;
    }

    public void setObjectiveId(String objectiveId) {
        this.objectiveId = objectiveId;
    }
}
