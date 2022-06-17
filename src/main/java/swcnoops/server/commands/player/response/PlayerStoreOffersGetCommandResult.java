package swcnoops.server.commands.player.response;

import swcnoops.server.requests.AbstractCommandResult;

public class PlayerStoreOffersGetCommandResult extends AbstractCommandResult {
    public Object availableOffer;
    public int globalCooldownExpiresAt;
    public Object nextOfferAvailableAt;
}
