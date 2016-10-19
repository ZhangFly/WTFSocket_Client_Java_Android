package wtf.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * Socket通信客户端
 */
class WTFSocketClient implements Runnable {

    private static Logger logger = Logger.getLogger("socket");

    // 传输终止符号
    static final String EOT = "\r\n";

    // 连接地址
    private WTFSocketConfig config;

    // socket 实例
    private Socket socket;

    // 框架轮询器
    private ScheduledExecutorService frameSchedule = Executors.newScheduledThreadPool(4);

    // 缓冲器
    private StringBuffer buffer = new StringBuffer();

    WTFSocketClient(WTFSocketConfig config) {
        this.config = config;
    }

    /**
     * 自举线程
     * 应为 android 不允许在主线程中开启 socket
     */
    @Override
    public void run() {

        try {
            if (socket == null) {
                // 新建socket
                socket = new Socket();
            } else {
                // 复位socket
                if (!socket.isClosed()) {
                    close();
                }
            }

            // 开启超时检查
            frameSchedule.scheduleAtFixedRate(new WTFSocketCheckTimeoutThread(), 0, 200, TimeUnit.MILLISECONDS);

            // 连接socket
            socket.connect(new InetSocketAddress(config.getIp(), config.getPort()), 5_000);
            socket.setKeepAlive(true);
            logger.info(String.format("socket connected!\nremote address => %s\nlocal address => %s", socket.getRemoteSocketAddress(), socket.getLocalSocketAddress()));

            // 开启写线程
            frameSchedule.scheduleAtFixedRate(new WTFSocketSendThread(this), 50, 200, TimeUnit.MILLISECONDS);
            // 开启接收监听线程
            frameSchedule.scheduleAtFixedRate(new WTFSocketReceiveThread(this), 100, 200, TimeUnit.MILLISECONDS);

            // 如果需要开启心跳包线程
            if (config.isUseHeartbeat()) {
                frameSchedule.scheduleAtFixedRate(new WTFSocketHeartbeatThread(config.getHeartbeatPeriod() * config.getHeartbeatBreakTime()), 150, config.getHeartbeatPeriod(), TimeUnit.MILLISECONDS);
            }

            WTFSocketSessionFactory.setIsAvailable(true);

            for (WTFSocketEventListener listener : WTFSocketSessionFactory.getEventListeners()) {
                listener.onConnect();
            }

        } catch (IOException e) {
            WTFSocketException exception = new WTFSocketException(e.getMessage());
            exception.setLocation(this.getClass().getName() + "$run");
            exception.setMsg(new WTFSocketMsgWrapper());
            WTFSocketSessionFactory.dispatchException(exception);
        }
    }

    // 开启客户端
    void start() {
        frameSchedule.execute(this);
    }

    // 关闭客户端
    void close() {
        try {
            frameSchedule.shutdown();
            socket.close();
        } catch (IOException e) {
            WTFSocketException exception = new WTFSocketException(e.getMessage());
            exception.setLocation(this.getClass().getName() + "$close");
            exception.setMsg(new WTFSocketMsgWrapper());
            WTFSocketSessionFactory.dispatchException(exception);
        }
    }

    // 解析并返回有效数据包
    synchronized List<String> parseAndGetPackets(String data) throws IOException {

        List<String> packets = new ArrayList<>();
        buffer.append(data);

        while (true) {
            int index = buffer.indexOf(WTFSocketClient.EOT);
            if (index == -1) {
                break;
            }
            String packet = buffer.substring(0, index);
            buffer.delete(0, index + 2);
            packets.add(packet);
        }

        return packets;
    }

    // 清空buffer
    synchronized void clearBuffer() {
        buffer.setLength(0);
    }

    // 获取socket实例
    Socket getSocket() {
        return socket;
    }
}
