package wtf.socket;

import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class WTFSocketLogUtils {

    private static Integer rootCount = null;

    private static String msgWrapper(String level, String msg) {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();

        if (rootCount == null) {
            for (rootCount = 0; rootCount < elements.length ; rootCount++) {
                if (StringUtils.equals(elements[rootCount].getMethodName(), "msgWrapper")) {
                    rootCount += 2;
                    break;
                }
            }
        }

        StringBuffer msgWrapper = new StringBuffer()
                .append(level)
                .append("  ")
                .append(new SimpleDateFormat("yyyy/MM/dd hh:mm:ss").format(new Date()))
                .append("  ")
                .append(elements[rootCount].getClassName())
                .append("$")
                .append(elements[rootCount].getMethodName())
                .append(":\n")
                .append(msg)
                .append("\n");
        return msgWrapper.toString();
    }

    public static void info(String msg) {

        System.out.print(msgWrapper("[INFO ]", msg));
    }

    public static void err(String msg) {

        System.err.print(msgWrapper("[ERROR]", msg));
    }

}
