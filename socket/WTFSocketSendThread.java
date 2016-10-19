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

    private WTFSocketBootstrapThread wtfSocketClient;

    WTFSocketSendThread(WTFSocketBootstrapThread socket) {
        this.wtfSocketClient = socket;
    }

    @Override
    public void run() {

        WTFSocketMsgWrapper msgWrapper = new WTFSocketMsgWrapper();

        try {
            // 检查是否有等待回复的消息超时
            WTFSocketSessionFactory.SERVER.checkResponseTimeout();
            // 检查是否有等待发送的消息超时
            WTFSocketSessionFactory.SERVER.checkSendTimeout();
            if (WTFSocketSessionFactory.SERVER.hasWaitSendMsg()) {
                // 发送可用消息超时
                doWrite(WTFSocketSessionFactory.SERVER);
            }

            for (WTFSocketSession session : WTFSocketSessionFactory.getSessions()) {

                // 服务器会话已优先处理
                if (session == WTFSocketSessionFactory.SERVER) {
                    continue;
                }

                // 检查是否有等待回复的消息超时
                session.checkResponseTimeout();
                // 检查是否有等待发送的消息超时
                session.checkSendTimeout();
                if (session.hasWaitSendMsg()) {
                    // 发送可用消息超时
                    doWrite(session);
                }
            }
        } catch (IOException e) {
            WTFSocketSessionFactory.dispatchException(new WTFSocketException(e.getMessage()));
        }

    }


    private void doWrite(WTFSocketSession session) throws IOException {

        Socket socket = wtfSocketClient.getSocket();
        // 等待发送的消息
        ConcurrentLinkedQueue<WTFSocketMsgWrapper> waitSendMsg = session.getWaitSendMsg();

        // 等待回复的消息
        ConcurrentHashMap<String, WTFSocketMsgWrapper> waitResponseMsg = session.getWaitResponseMsg();

        // 需要从发送队列中移除的消息
        List<WTFSocketMsgWrapper> sentMsg = new ArrayList<>();

        for (WTFSocketMsgWrapper msgWrapper : waitSendMsg) {

            if (socket.isClosed() || !socket.isConnected()) {

                break;
            }

            if (msgWrapper.getMsgType() != 0) {
                logger.info(String.format("send msg from <%s> to <%s>:\nmsg => %s",
                        msgWrapper.getFrom(),
                        msgWrapper.getTo(),
                        msgWrapper));
            }

            socket.getOutputStream().write((msgWrapper + WTFSocketBootstrapThread.EOT).getBytes());
            sentMsg.add(msgWrapper);
        }

        for (WTFSocketMsgWrapper msgWrapper : sentMsg) {
            waitSendMsg.remove(msgWrapper);
            if (msgWrapper.isNeedResponse()) {
                waitResponseMsg.put(msgWrapper.getTag(), msgWrapper);
            }
        }
    }
}
