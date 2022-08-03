package swcnoops.server;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.utils.IOUtils;
import javax.servlet.ServletOutputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

import static spark.Spark.*;
import static swcnoops.server.requests.BatchProcessorImpl.decodeParameters;

public class SparkMain {
    public static void main(String[] args) {
        initialise();
        port(8080);
        BatchRoute batchRoute = new BatchRoute();
        post("/starts/batch/json", batchRoute);
        post("/starts/batch/jsons", batchRoute);
        post("/bi_event2", (a,b) -> {b.type("octet-stream"); return "{}";});
        get("/swcFiles/*", new GetFile());
        get("/*", new ConnectionTest());

        exception(Exception.class, (e, request, response) -> {
            e.printStackTrace();
            response.status(404);
            response.body("Resource not found");
        });
    }

    private static void initialise() {
        Config config = new Config();
        ServiceFactory.instance(config);
        ServiceFactory.instance().getPlayerDatasource().initOnStartup();
        ServiceFactory.instance().getGameDataManager().initOnStartup();
    }

    private static class BatchRoute implements Route {
        @Override
        public Object handle(Request request, Response response) throws Exception {
            String postBody = request.body();
            Map<String, List<String>> parameters = decodeParameters(postBody);
            String batchRequestJson = parameters.get("batch").get(0);
            String json = ServiceFactory.instance().getBatchProcessor().processBatchPostBody(batchRequestJson);
            response.type("application/json");
            response.body(json);
            return json;
        }
    }

    private static class GetFile implements Route {
        @Override
        public Object handle(Request request, Response response) throws Exception {
            String path = ServiceFactory.instance().getConfig().swcRootPath + request.pathInfo();
            //long fileSize = Files.size(Paths.get(path));
            response.type("octet-stream");
            //response.raw().setContentLength((int) fileSize);
            response.status(200);

            final ServletOutputStream os = response.raw().getOutputStream();
            final InputStream in = new BufferedInputStream(new FileInputStream(path));
            IOUtils.copy(in, os);
            in.close();
            os.close();
            return null;
        }
    }

    private static class ConnectionTest implements Route {
        @Override
        public Object handle(Request request, Response response) throws Exception {
            if (request.pathInfo() != null && request.pathInfo().contains("connection_test.txt")) {
                response.type("octet-stream");
                response.status(200);
                return "success";
            }

            response.status(404);
            return null;
        }
    }
}
