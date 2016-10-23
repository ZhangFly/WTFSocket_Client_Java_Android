package wtf.socket;
/**
 * socket连接配置
 */
public class WTFSocketConfig {

    private String ip;
    private int port;
    private String localName;
    private boolean useHeartbeat = false;
    private int heartbeatPeriod = 5_000;
    private int heartbeatBreakTime = 3;

    public String getIp() {
        return ip;
    }

    public WTFSocketConfig setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public int getPort() {
        return port;
    }

    public WTFSocketConfig setPort(int port) {
        this.port = port;
        return this;
    }

    public String getLocalName() {
        return localName;
    }

    public WTFSocketConfig setLocalName(String localName) {
        this.localName = localName;
        return this;
    }

    public boolean isUseHeartbeat() {
        return useHeartbeat;
    }

    public WTFSocketConfig setUseHeartbeat(boolean useHeartbeat) {
        this.useHeartbeat = useHeartbeat;
        return this;
    }

    public int getHeartbeatPeriod() {
        return heartbeatPeriod;
    }

    public WTFSocketConfig setHeartbeatPeriod(int heartbeatPeriod) {
        this.heartbeatPeriod = heartbeatPeriod;
        return this;
    }

    public int getHeartbeatBreakTime() {
        return heartbeatBreakTime;
    }

    public WTFSocketConfig setHeartbeatBreakTime(int heartbeatBreakTime) {
        this.heartbeatBreakTime = heartbeatBreakTime;
        return this;
    }
}
