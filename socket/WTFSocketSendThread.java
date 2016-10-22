package socket;

import java.io.IOException;
import java.net.Socket;

class WTFSocketSendThread implements Runnable {

    private WTFSocketBootstrap wtfSocketClient;

    WTFSocketSendThread(WTFSocketBootstrap socket) {
        this.wtfSocketClient = socket;
    }

    @Override
    public void run() {

        // 检查是否有等待回复的消息超时
        WTFSocketSessionFactory.SERVER.checkResponseMsgTimeout();

        if (WTFSocketSessionFactory.SERVER.hasWaitSendMsg()) {
            doWrite(WTFSocketSessionFactory.SERVER);
        }

        for (WTFSocketSession session : WTFSocketSessionFactory.getSessions()) {

            // 服务器会话已优先处理
            if (session == WTFSocketSessionFactory.SERVER) {
                continue;
            }

            // 检查是否有等待回复的消息超时
            session.checkResponseMsgTimeout();
            if (session.hasWaitSendMsg()) {
                doWrite(session);
            }
        }
    }


    private void doWrite(WTFSocketSession session) {

        Socket socket = wtfSocketClient.getSocket();

        WTFSocketMsgWrapper msgWrapper = session.nextWaitSendMsg();
        try {

            while (msgWrapper != null) {

                if (!socket.isClosed() && socket.isConnected()) {
                    if (msgWrapper.getMsgType() != 0) {
                        WTFSocketLogUtils.info(String.format(
                                "send msg from <%s> to <%s>:\r\nmsg => %s",
                                msgWrapper.getFrom(),
                                msgWrapper.getTo(),
                                msgWrapper
                        ));
                    }
                    socket.getOutputStream().write((msgWrapper + WTFSocketBootstrap.EOT).getBytes());
                }else{
                    session.rollbackSendMsg(msgWrapper);
                }

                msgWrapper = session.nextWaitSendMsg();
            }

        } catch (IOException e) {
            session.rollbackSendMsg(msgWrapper);
            WTFSocketSessionFactory.dispatchException(new WTFSocketException(e.getMessage()), msgWrapper);
        }
    }
}
