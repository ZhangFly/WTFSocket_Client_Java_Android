package wtf.socket;

/**
 * socket异常类
 */
public class WTFSocketException extends Exception{

    private String msg;

    WTFSocketException(String msg) {

        StackTraceElement element = Thread.currentThread().getStackTrace()[2];
        int lineNo = element.getLineNumber();
        this.msg = String.format(
                "where => %s$%s\ncause => %s",
                element.getClassName(),
                element.getMethodName(),
                msg);

    }

    @Override
    public String getMessage() {
        return msg;
    }

}
