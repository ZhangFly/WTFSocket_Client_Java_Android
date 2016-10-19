package wtf.socket;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

class WTFSocketSendThread implements Runnable {

    private static final Logger logger = Logger.getLogger("socket");

    private WTFSocketClient wtfSocketClient;

    WTFSocketSendThread(WTFSocketClient socket) {
        this.wtfSocketClient = socket;
    }

    @Override
    public void run() {

        WTFSocketMsgWrapper msgWrapper = new WTFSocketMsgWrapper();

        try {
            if (WTFSocketSessionFactory.SERVER.hasWaitSendMsg()) {
                // 检查是否有等待回复的消息超时
                WTFSocketSessionFactory.SERVER.checkResponseTimeout();
                if (WTFSocketSessionFactory.SERVER.hasWaitSendMsg()) {
                    // 检查是否有等待发送的消息超时
                    WTFSocketSessionFactory.SERVER.checkSendTimeout();
                    // 发送可用消息超时
                    doWrite(WTFSocketSessionFactory.SERVER);
                }
            }

            for (WTFSocketSession session : WTFSocketSessionFactory.getSessions()) {

                // 服务器会话已优先处理
                if (session == WTFSocketSessionFactory.SERVER) {
                    break;
                }

                // 检查是否有等待回复的消息超时
                session.checkResponseTimeout();
                if (session.hasWaitSendMsg()) {
                    // 检查是否有等待发送的消息超时
                    session.checkSendTimeout();
                    // 发送可用消息超时
                    doWrite(session);
                }
            }

        } catch (IOException e) {
            WTFSocketException exception = new WTFSocketException(e.getMessage());
            exception.setLocation(this.getClass().getName() + "$run");
            WTFSocketSessionFactory.dispatchException(msgWrapper, exception);
        }

    }


    private void doWrite(WTFSocketSession session) throws IOException {

        Socket socket = wtfSocketClient.getSocket();
        // 等待发送的消息
        ConcurrentLinkedQueue<WTFSocketMsgWrapper> waitSendMsg = session.getWaitSendMsg();

        // 等待回复的消息
        ConcurrentHashMap<String, WTFSocketMsgWrapper> waitResponseMsg = session.getWaitResponseMsg();

        // 需要从发送队列中移除的消息
        List<WTFSocketMsgWrapper> toRemove = new ArrayList<>();

        for (WTFSocketMsgWrapper msgWrapper : waitSendMsg) {

            if (socket.isClosed() || !socket.isConnected()) {

                break;
            }

            if (msgWrapper.getMsgType() != 0) {
                logger.info(String.format("sendMsg msg from <%s> to <%s>:\nmsg => %s",
                        msgWrapper.getFrom(),
                        msgWrapper.getTo(),
                        msgWrapper));
            }

            socket.getOutputStream().write((msgWrapper + WTFSocketClient.EOT).getBytes());
            toRemove.add(msgWrapper);
        }

        for (WTFSocketMsgWrapper msgWrapper : toRemove) {
            waitSendMsg.remove(msgWrapper);
            if (msgWrapper.isNeedResponse()) {
                waitResponseMsg.put(msgWrapper.getTag(), msgWrapper);
            }
        }
    }
}
