package swcnoops.server.session;

import swcnoops.server.ServiceFactory;
import swcnoops.server.requests.Batch;
import swcnoops.server.requests.BatchResponse;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BatchResponseReplayer {
    private Map<String, BatchInfo> batchResponses = new ConcurrentHashMap<>();
    private BatchAction authBatchAction = new SessionBatchAction() {
        @Override
        public boolean canSaveResponse() {
            return false;
        }

        @Override
        public boolean shouldWaitForResponse() {
            return false;
        }
    };

    private BatchAction continueBatchAction = new SessionBatchAction() {
        @Override
        public boolean canSaveResponse() {
            return true;
        }

        @Override
        public boolean shouldWaitForResponse() {
            return false;
        }
    };

    public BatchAction waitOrProcessBatch(Batch batch) {
        if (!ServiceFactory.instance().getConfig().enableBatchResponseReplayer)
            return this.authBatchAction;

        if (batch.getAuthKey() == null || batch.getAuthKey().isEmpty())
            return this.authBatchAction;

        BatchInfo batchInfo = this.batchResponses.get(batch.getAuthKey());
        if (batchInfo == null) {
            synchronized (this.batchResponses) {
                batchInfo = this.batchResponses.get(batch.getAuthKey());
                if (batchInfo == null) {
                    batchInfo = new BatchInfo(batch);
                    this.batchResponses.put(batch.getAuthKey(), batchInfo);
                    return new SessionBatchAction(batchInfo);
                }
            }
        }

        return batchInfo.determineAction(batch);
    }

    public void saveResponse(Batch batch, BatchResponse batchResponse, Exception exceptionResponse) {
        BatchInfo batchInfo = this.batchResponses.get(batch.getAuthKey());
        if (batchInfo == null) {
            throw new RuntimeException("Failed to save batch response");
        }

        batchInfo.saveResponse(batch, batchResponse, exceptionResponse);
    }

    public class BatchInfo {
        final private String authKey;
        public Set<Long> processingSet = new HashSet<>();
        private long currentMinRequestId = 0;
        private long currentMaxRequestId = 0;

        private Map<Long, SavedResponse> savedResponseMap = new ConcurrentHashMap<>();

        public BatchInfo(Batch batch) {
            this.authKey = batch.getAuthKey();
            if (batch.getCommands() != null && batch.getCommands().size() > 0) {
                this.currentMinRequestId = batch.getCommands().get(0).getRequestId();
                this.currentMaxRequestId = batch.getCommands().get(batch.getCommands().size()-1).getRequestId();
            }
        }

        public long getCurrentMaxRequestId() {
            return currentMaxRequestId;
        }

        synchronized protected void saveResponse(Batch batch, BatchResponse batchResponse, Exception exceptionResponse) {
            if (batch.getCommands() != null && batch.getCommands().size() > 0) {
                this.currentMinRequestId = batch.getCommands().get(0).getRequestId();
                this.currentMaxRequestId = batch.getCommands().get(batch.getCommands().size()-1).getRequestId();
            }

            this.savedResponseMap.put(this.currentMinRequestId, new SavedResponse(batchResponse, exceptionResponse));
            this.processingSet.remove(this.currentMinRequestId);
            this.notifyAll();
        }

        synchronized public SavedResponse waitForResponse(long minRequestId, int waitTime) {
            long waitUntil = System.currentTimeMillis() + waitTime;
            while (!this.savedResponseMap.containsKey(minRequestId) && System.currentTimeMillis() < waitUntil) {
                try {
                    this.wait(waitTime);
                } catch (InterruptedException ex) {
                }
            }

            return this.savedResponseMap.get(minRequestId);
        }

        synchronized public BatchAction determineAction(Batch batch) {
            long minRequestId = batch.getCommands().get(0).getRequestId();

            // see if the request is running out of order and if it is check to see if being processed
            if (minRequestId != this.getCurrentMaxRequestId() + 1) {
                if (!this.savedResponseMap.containsKey(minRequestId)) {
                    if (!this.processingSet.contains(minRequestId)) {
                        this.processingSet.add(minRequestId);
                        return continueBatchAction;
                    }
                }

                return new WaitForResponse(this, minRequestId);
            }

            // we are in sync so clear previous requests
            this.savedResponseMap.clear();
            this.processingSet.clear();
            this.processingSet.add(minRequestId);
            return continueBatchAction;
        }
    }

    private class SavedResponse {
        private BatchResponse batchResponse;
        private Exception exceptionResponse;
        public SavedResponse(BatchResponse batchResponse, Exception exceptionResponse) {
            this.batchResponse = batchResponse;
            this.exceptionResponse = exceptionResponse;
        }
    }

    private class SessionBatchAction implements BatchAction {
        final protected BatchInfo batchInfo;

        public SessionBatchAction() {
            this(null);
        }

        public SessionBatchAction(BatchInfo batchInfo) {
            this.batchInfo = batchInfo;
        }

        @Override
        public boolean canSaveResponse() {
            return true;
        }

        @Override
        public boolean shouldWaitForResponse() {
            return false;
        }

        @Override
        public void waitForResponse(int i) {

        }

        @Override
        public boolean hasResponse() {
            return false;
        }

        @Override
        public BatchResponse getResponse() {
            return null;
        }

        @Override
        public boolean hasException() {
            return false;
        }

        @Override
        public Exception getException() {
            return null;
        }
    }

    private class WaitForResponse extends SessionBatchAction {
        final private long minRequestId;
        private SavedResponse savedResponse;
        public WaitForResponse(BatchInfo batchInfo, long minRequestId) {
            super(batchInfo);
            this.minRequestId = minRequestId;
        }

        @Override
        public boolean shouldWaitForResponse() {
            return true;
        }

        @Override
        public void waitForResponse(int waitTime) {
            this.savedResponse = this.batchInfo.waitForResponse(this.minRequestId, waitTime);
        }

        @Override
        public boolean hasResponse() {
            return this.savedResponse != null && this.savedResponse.batchResponse != null;
        }

        @Override
        public BatchResponse getResponse() {
            return this.savedResponse.batchResponse;
        }

        @Override
        public boolean hasException() {
            return this.savedResponse != null && this.savedResponse.exceptionResponse != null;
        }

        @Override
        public Exception getException() {
            return this.savedResponse.exceptionResponse;
        }
    }
}
