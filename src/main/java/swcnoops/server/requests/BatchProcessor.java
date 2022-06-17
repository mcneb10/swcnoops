package swcnoops.server.requests;

public interface BatchProcessor extends BatchDecoder {
    BatchResponse executeCommands(Batch batch) throws Exception;

    String processBatchPostBody(String postBody) throws Exception;
}
