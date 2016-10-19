package wtf.socket;

/**
 * socket异常类
 */
public class WTFSocketException extends Exception{

    private String location;

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
}
