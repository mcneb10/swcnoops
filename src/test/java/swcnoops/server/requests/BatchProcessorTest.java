package swcnoops.server.requests;

import swcnoops.server.Config;
import swcnoops.server.ServiceFactory;
import swcnoops.server.requests.Batch;
import swcnoops.server.requests.BatchProcessor;
import swcnoops.server.requests.BatchProcessorImpl;
import swcnoops.server.requests.BatchResponse;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BatchProcessorTest {

    static {
        ServiceFactory.instance(new Config());
    }

    @Test
    public void batchProcessorTest() throws Exception {
        String batchRequest = "{\"authKey\":\"\",\"pickupMessages\":true,\"lastLoginTime\":0,\"commands\":[{\"action\":\"auth.getAuthToken\",\"args\":{\"playerId\":\"afcac7e6-9d1f-11e9-8877-0a580a2c1661\",\"requestToken\":\"MTg0RkFBRDIwNUY0ODg5MkEzMDQ0OUIwNDFGODg1MUZENTI1MzUyNzMxMEU3N0MzRTdEQ0M4NzA3MUIxQjJCQi57InVzZXJJZCI6IjlmZjY4OGQ5LTkxYWItMTFlOS05ZTI1LTBhNTgwYTJjMTQ0OSIsImV4cGlyZXMiOjE2NTQ5MDU1MTk2OTh9\",\"deviceType\":\"a\"},\"requestId\":2,\"time\":0,\"token\":\"0bc1e26b-227c-4d8f-bd6e-588813f139f4\"}]}";
        batchRequest = "{\"authKey\":\"\",\"pickupMessages\":true,\"lastLoginTime\":0,\"commands\":[{\"action\":\"auth.preauth.generatePlayer\",\"args\":{\"locale\":\"en_US\"},\"requestId\":2,\"time\":0,\"token\":\"9cf2d3ec-93db-4a25-8140-e3d1f0d5b323\"}]}";

        BatchProcessor batchProcessor = new BatchProcessorImpl();
        Batch batch = batchProcessor.decode(batchRequest);
        assertEquals(1, batch.getCommands().size());

        batchProcessor.decodeCommands(batch);
        assertNotNull(batch.getCommands().get(0).getCommandAction());

        BatchResponse batchResponse = batchProcessor.executeCommands(batch);
        assertNotNull(batch.getCommands().get(0).getResponse());
        assertNotNull(batchResponse);
        String json = ServiceFactory.instance().getJsonParser().toJson(batchResponse);
        assertNotNull(json);
    }
}
