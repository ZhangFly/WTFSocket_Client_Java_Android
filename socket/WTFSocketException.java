package wtf.socket;

/**
 * socket异常类
 */
public class WTFSocketException extends Exception{

    private StringBuffer format;

    private String addition;

    WTFSocketException(String msg) {

        StackTraceElement element = Thread.currentThread().getStackTrace()[2];

        format = new StringBuffer()
                .append("where => ")
                .append(element.getClassName())
                .append("$")
                .append(element.getMethodName())
                .append("\r\ncause => ")
                .append(msg);

    }

    @Override
    public String getMessage() {
        if (addition != null) {
            format.append("\r\naddition => data: ")
                    .append(addition);
        }
        return format.toString();
    }

    @Override
    public String toString() {
        return getMessage();
    }

    public String getAddition() {
        return addition;
    }

    public WTFSocketException setAddition(String addition) {
        this.addition = addition;
        return this;
    }
}
