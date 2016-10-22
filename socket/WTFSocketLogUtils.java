package socket;

import java.text.SimpleDateFormat;
import java.util.Date;

public class WTFSocketLogUtils {

    private static String msgWrapper(String level, String msg) {
        StackTraceElement[] element = Thread.currentThread().getStackTrace();
        StringBuffer msgWrapper = new StringBuffer()
                .append(level)
                .append("  ")
                .append(new SimpleDateFormat("yyyy/MM/dd hh:mm:ss").format(new Date()))
                .append("  ")
                .append(element[3].getClassName())
                .append("$")
                .append(element[3].getMethodName())
                .append(":\n")
                .append(msg);
        return msgWrapper.toString();
    }

    public static void info(String msg) {

        System.out.println(msgWrapper("[INFO ]", msg));
    }

    public static void err(String msg) {

        System.err.println(msgWrapper("[ERROR]", msg));
    }

}
