package swcnoops.server.requests;

import swcnoops.server.ServiceFactory;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BatchResponse {
    static final private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'+01:00'");

    private Integer protocolVersion;
    final private List<ResponseData> data;
    private String serverTime;
    private Long serverTimestamp;
    public BatchResponse(List<ResponseData> responseDatums) {
        this.data = responseDatums;

        setProtocolVersion(ServiceFactory.instance().getConfig().PROTOCOL_VERSION);
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        setServerTimestamp(zonedDateTime.toEpochSecond());
        setServerTime(dateTimeFormatter.format(zonedDateTime));
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
    private void setProtocolVersion(Integer protocolVersion) {
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
