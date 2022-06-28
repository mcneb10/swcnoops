package swcnoops.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class Main extends NanoHTTPD {
    public Main() throws IOException {
        super(ServiceFactory.instance().getConfig().webServicePort);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("\nRunning on port " + ServiceFactory.instance().getConfig().webServicePort);
    }

    public static void main(String[] args) {
        try {
            initialise();
            new Main();
        } catch (IOException ioe) {
            System.err.println("Couldn't start server:\n" + ioe);
        }
    }

    private static void initialise() {
        // TODO - load in config and init serviceFactory
        Config config = new Config();
        ServiceFactory.instance(config);
        // TODO - should have a nicer way to init on startup and register to factory
        ServiceFactory.instance().getPlayerDatasource().initOnStartup();
        ServiceFactory.instance().getGameDataManager().initOnStartup();
    }

    @Override
    public Response serve(IHTTPSession session) {
        Response response = null;

        try {
            // TODO - look at the URL to determine what needs to be processed
            if (session.getUri().contains("connection_test")) {
                response = processConnectionTest(session);
            } else if (session.getUri().contains("bi_event2")) {
                response = processBIEvent(session);
            } else if (session.getUri().contains("batch/json")) {
                String postBody = getPostBody(session);
                response = processBatchPostBody(postBody);
            } else if (session.getUri().contains(ServiceFactory.instance().getConfig().swcFolderName)) {
                // loading swc files
                String fullPath = ServiceFactory.instance().getConfig().swcRootPath + session.getUri();
                // TODO - check if files exists and only if for swcFiles
                FileInputStream fis = null;
                long fileSize = 0;
                try {
                    fis = new FileInputStream(fullPath);
                    fileSize = Files.size(Paths.get(fullPath));
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                response = newChunkedResponse(Response.Status.OK, "application/octet-stream", fis);
                //response = newFixedLengthResponse(Response.Status.OK, "application/octet-stream", fis, fileSize);
            } else {
                if (session.getHeaders().containsKey("bugsnag-api-key"))
                    response = newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "success");
                else
                    response = newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "bad");
            }
        } catch(Exception ex) {
            // TODO - should log here
            System.out.println(ex);
            ex.printStackTrace();
            response = newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "bad");
        }

        return response;
    }

    private Response processBatchPostBody(String postBody) throws Exception {
        String json = ServiceFactory.instance().getBatchProcessor().processBatchPostBody(postBody);
        Response response = newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json;charset=UTF-8", json);
        return response;
    }

    private String getPostBody(IHTTPSession session) throws Exception {
        Map<String, String> files = new HashMap<String, String>();
        Method method = session.getMethod();
        String batchRequestJson = null;
        if (Method.PUT.equals(method) || Method.POST.equals(method)) {
            session.parseBody(files);
            // get the POST body
            String postBody = session.getQueryParameterString();
            Map<String, List<String>> parameters = decodeParameters(postBody);

            if (parameters.containsKey("batch"))
                batchRequestJson = parameters.get("batch").get(0);
        }

        return batchRequestJson;
    }

    private Response processConnectionTest(IHTTPSession session) {
        return newFixedLengthResponse("success");
    }

    private Response processBIEvent(IHTTPSession session) {
        return newFixedLengthResponse("{}");
    }
}