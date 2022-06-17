package swcnoops.server.requests;

import swcnoops.server.Config;
import swcnoops.server.ServiceFactory;
import swcnoops.server.requests.Batch;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BatchTest {
    static {
        ServiceFactory.instance(new Config());
    }
    @Test
    public void testBatchParse() throws Exception {
        ServiceFactory.instance(new Config());

        String json = "{\"authKey\":\"\",\"pickupMessages\":true,\"lastLoginTime\":0,\"commands\":[{\"action\":\"auth.preauth.generatePlayer\",\"args\":{\"locale\":\"en_US\"},\"requestId\":2,\"time\":0,\"token\":\"aaaaaec-93db-4a25-8140-e3d1f0d5b323\"}]}";
        Batch batch = ServiceFactory.instance().getJsonParser().fromJsonString(json, Batch.class);
        System.out.println(batch);
    }
}
