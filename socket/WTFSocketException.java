package wtf.socket;

/**
 * socket异常类
 */
public class WTFSocketException extends Exception{

    private String location;

    private WTFSocketMsgWrapper msg;

    WTFSocketException(String msg) {
        super(msg);
    }

    public String getLocation() {
        return location;
    }

    WTFSocketException setLocation(String location) {
        this.location = location;
        return this;
    }

    WTFSocketMsgWrapper getMsg() {
        return msg;
    }

    WTFSocketException setMsg(WTFSocketMsgWrapper msg) {
        this.msg = msg;
        return this;
    }
}
