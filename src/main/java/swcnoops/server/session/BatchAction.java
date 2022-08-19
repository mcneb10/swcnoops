package swcnoops.server.session;

import swcnoops.server.requests.BatchResponse;

public interface BatchAction {
    boolean canSaveResponse();

    boolean shouldWaitForResponse();

    void waitForResponse(int i);

    boolean hasResponse();

    BatchResponse getResponse();

    boolean hasException();

    Exception getException();
}
