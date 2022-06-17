package swcnoops.server.requests;

public interface BatchDecoder {
    Batch decode(String batchRequestJson) throws Exception;

    void decodeCommands(Batch batch) throws Exception;
}
