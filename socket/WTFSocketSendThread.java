package wtf.socket;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

class WTFSocketSendThread implements Runnable {

    private static final Logger logger = Logger.getLogger("socket");

    private WTFSocketBootstrap wtfSocketClient;

    WTFSocketSendThread(WTFSocketBootstrap socket) {
        this.wtfSocketClient = socket;
    }

    @Override
    public void run() {

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
    }


    private void doWrite(WTFSocketSession session) {

        Socket socket = wtfSocketClient.getSocket();
        // 等待发送的消息
        ConcurrentHashMap<String, WTFSocketMsgWrapper> waitSendMsg = session.getWaitSendMsg();

        // 等待回复的消息
        ConcurrentHashMap<String, WTFSocketMsgWrapper> waitResponseMsg = session.getWaitResponseMsg();

        // 需要从发送队列中移除的消息
        List<WTFSocketMsgWrapper> sentMsg = new ArrayList<>();

        WTFSocketMsgWrapper msgWrapper = new WTFSocketMsgWrapper();

        try {

            for (WTFSocketMsgWrapper wrapper : waitSendMsg.values()) {

                if (socket.isClosed() || !socket.isConnected()) {
                    break;
                }

                msgWrapper = wrapper;

                if (wrapper.getMsgType() != 0) {
                    logger.info(String.format(
                            "send msg from <%s> to <%s>:\r\nmsg => %s",
                            wrapper.getFrom(),
                            wrapper.getTo(),
                            wrapper
                    ));
                }

                socket.getOutputStream().write((wrapper + WTFSocketBootstrap.EOT).getBytes());
                sentMsg.add(wrapper);
            }

            for (WTFSocketMsgWrapper wrapper : sentMsg) {
                waitSendMsg.remove(wrapper.getTag());
                if (wrapper.isNeedResponse()) {
                    waitResponseMsg.put(wrapper.getTag(), wrapper);
                }
            }

        } catch (IOException e) {
            WTFSocketSessionFactory.dispatchException(new WTFSocketException(e.getMessage()), msgWrapper);
        }
    }
}
