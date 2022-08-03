package swcnoops.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.utils.IOUtils;
import javax.servlet.ServletOutputStream;
import java.io.*;
import java.util.*;

import static spark.Spark.*;
import static swcnoops.server.requests.BatchProcessorImpl.decodeParameters;

public class SparkMain {
    private static final Logger LOG = LoggerFactory.getLogger(SparkMain.class);

    public static void main(String[] args) {
        try {
            LOG.info("Initialising");
            Properties properties = readProperties(args.length >= 1 ? args[0] : null);
            initialise(properties);
            LOG.info("Initialising complete, going to setup webserver and routes");
            port(ServiceFactory.instance().getConfig().webServicePort);
            BatchRoute batchRoute = new BatchRoute();
            post("/starts/batch/json", batchRoute);
            post("/starts/batch/jsons", batchRoute);
            post("/bi_event2", (a, b) -> {
                b.type("octet-stream");
                return "{}";
            });
            get("/swcFiles/*", new GetFile());
            get("/*", new ConnectionTest());

            exception(Exception.class, (e, request, response) -> {
                LOG.error("Caught exception ", e);
                response.status(404);
                response.body("Resource not found");
            });

            LOG.info("Done ready to process from port " + ServiceFactory.instance().getConfig().webServicePort);

        } catch (Exception ex) {
            LOG.error("Failed with exception", ex);
            System.exit(-1);
        }
    }

    private static void initialise(Properties properties) throws Exception {
        Config config = new Config();
        config.setFromProperties(properties);
        ServiceFactory.instance(config);
        ServiceFactory.instance().getPlayerDatasource().initOnStartup();
        ServiceFactory.instance().getGameDataManager().initOnStartup();
    }

    private static Properties readProperties(String fileName) throws Exception {
        Properties prop = new Properties();
        if (fileName != null) {
            LOG.info("Reading properties file " + fileName);
            File configFile = new File(fileName);
            try (FileReader reader = new FileReader(configFile)) {
                prop.load(reader);
            }
        }

        return prop;
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
            return "";
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
            return "";
        }
    }
}
