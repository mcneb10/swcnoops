package swcnoops.server.session;

import swcnoops.server.session.training.BuildUnit;

public interface Constructor {
    void removeCompletedBuildUnit(BuildUnit buildUnit);
}
