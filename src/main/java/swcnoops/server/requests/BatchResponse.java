package swcnoops.server.requests;

import java.util.List;

public class BatchResponse {
    private Integer protocolVersion;
    final private List<ResponseData> data;
    private String serverTime;
    private Long serverTimestamp;
    public BatchResponse(List<ResponseData> responseDatums) {
        this.data = responseDatums;
    }

    /**
     * used by json parsers in the test
     */
    private BatchResponse() {
        this(null);
    }

    public Integer getProtocolVersion() {
        return protocolVersion;
    }
    public void setProtocolVersion(Integer protocolVersion) {
        this.protocolVersion = protocolVersion;
    }
    public List<ResponseData> getData() {
        return data;
    }
    public Long getServerTimestamp() {
        return serverTimestamp;
    }
    public void setServerTimestamp(Long serverTimestamp) {
        this.serverTimestamp = serverTimestamp;
    }
    public String getServerTime() {
        return serverTime;
    }
    public void setServerTime(String serverTime) {
        this.serverTime = serverTime;
    }
}
