package wtf.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
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
    // socket 接收线程
    private WTFSocketReceiveThread receiveThread = new WTFSocketReceiveThread();
    // socket 发送线程
    private WTFSocketSendThread sendThread = new WTFSocketSendThread();
    // socket 心跳线程
    private WTFSocketHeartbeatThread beatThread = new WTFSocketHeartbeatThread();


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

            socket.connect(new InetSocketAddress(config.getIp(), config.getPort()), 5_000);
            socket.setKeepAlive(true);
            logger.info(String.format("socket connected!\nremote address => %s\nlocal address => %s", socket.getRemoteSocketAddress(), socket.getLocalSocketAddress()));

            // 开启监听线程
            new Thread(receiveThread.bindSocket(socket)).start();
            // 开启写线程
            new Thread(sendThread.bindSocket(socket)).start();

            // 如果需要，开启心跳线程
            if (config.isUseHeartbeat()) {
                new Thread(beatThread.bindSocket(socket)).start();
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
        new Thread(this).start();
    }

    // 关闭客户端
    void close() {
        try {
            socket.close();
        } catch (IOException e) {
            WTFSocketException exception = new WTFSocketException(e.getMessage());
            exception.setLocation(this.getClass().getName() + "$close");
            exception.setMsg(new WTFSocketMsgWrapper());
            WTFSocketSessionFactory.dispatchException(exception);
        }
    }

}
