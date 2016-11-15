package wtf.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Socket通信客户端
 */
class WTFSocketBootstrap implements Runnable {

    // 自举方法实例
    private static WTFSocketBootstrap INSTANCE = null;

    // socket 实例
    private static Socket socket;

    // 框架轮询器
    private static ScheduledExecutorService frameSchedule = Executors.newSingleThreadScheduledExecutor();

    // 协议解释器
    private static WTFSocketProtocolParser parser = null;

    private WTFSocketBootstrap() {

        WTFSocketConfig config = WTFSocketSessionFactory.getConfig();

        // 写线程同时负责检查消息是否超时
        frameSchedule.scheduleAtFixedRate(new WTFSocketSendTask(), 50, 200, TimeUnit.MILLISECONDS);

        // 如果需要开启心跳包线程
        if (config.isUseHeartbeat()) {
            frameSchedule.scheduleAtFixedRate(
                    new WTFSocketHeartbeatTask(),
                    config.getHeartbeatPeriod(),
                    config.getHeartbeatPeriod(), TimeUnit.MILLISECONDS
            );
        }

        // 开启接收监听线程
        frameSchedule.scheduleAtFixedRate(new WTFSocketReceiveTask(), 150, 200, TimeUnit.MILLISECONDS);
    }

    /**
     * 自举线程
     * 应为 android 不允许在主线程中开启 socket
     */
    @Override
    public void run() {

        try {
            WTFSocketConfig config = WTFSocketSessionFactory.getConfig();

            parser = new WTFSocketProtocolParser(WTFSocketMsg.class);

            // 开启socket
            socket = new Socket();

            // 连接socket
            socket.connect(new InetSocketAddress(config.getIp(), config.getPort()), 5_000);
            socket.setKeepAlive(true);
            WTFSocketLogUtils.info(String.format("socket connected!\nremote address => %s\nlocal address => %s", socket.getRemoteSocketAddress(), socket.getLocalSocketAddress()));

            // 更新框架状态
            WTFSocketSessionFactory.setIsAvailable(true);

            WTFSocketSessionFactory.notifyEventListeners(WTFSocketEventType.CONNECT);

        } catch (IOException e) {
            WTFSocketSessionFactory.dispatchException(new WTFSocketIOException(e.getMessage()));
        }
    }

    // 开启客户端
    static void start() {
        if (INSTANCE == null) {
            INSTANCE = new WTFSocketBootstrap();
        }
        frameSchedule.execute(INSTANCE);
    }

    // 关闭客户端
    static void close() {
        try {
            // 关闭socket连接
            socket.close();
        } catch (Exception e) {
            WTFSocketSessionFactory.dispatchException(new WTFSocketIOException(e.getMessage()));
        }
    }

    // 终止客户端
    static void shutdown() {
        try {
            // 关闭socket连接
            socket.close();
            frameSchedule.shutdown();
        } catch (Exception e) {
            WTFSocketSessionFactory.dispatchException(new WTFSocketIOException(e.getMessage()));
        }
    }

    // 获取socket实例
    static Socket getSocket() {
        return socket;
    }

    static WTFSocketProtocolParser getParser() {
        return parser;
    }
}
