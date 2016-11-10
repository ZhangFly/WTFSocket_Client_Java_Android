package wtf.socket;

import org.apache.commons.lang.StringUtils;

/**
 * socket异常类
 */
public abstract class WTFSocketException extends Exception{

    private StringBuffer format;

    private String addition;

    private static Integer rootCount = null;

    WTFSocketException(String msg) {

        StackTraceElement[] elements = Thread.currentThread().getStackTrace();

        if (rootCount == null) {
            for (rootCount = 0; rootCount < elements.length ; rootCount++) {
                if (StringUtils.equals(elements[rootCount].getClassName(), WTFSocketException.class.getName())) {
                    rootCount += 2;
                    break;
                }
            }
        }

        format = new StringBuffer()
                .append("where => ")
                .append(elements[rootCount].getClassName())
                .append("$")
                .append(elements[rootCount].getMethodName())
                .append("\ncause => ")
                .append(msg);

    }

    @Override
    public String getMessage() {
        if (addition != null) {
            format.append("\naddition => data: ")
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
