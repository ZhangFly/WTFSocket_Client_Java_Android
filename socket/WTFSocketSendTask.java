package wtf.socket;

import org.apache.commons.lang.ObjectUtils;

import java.io.IOException;
import java.net.Socket;

class WTFSocketSendTask implements Runnable {


    @Override
    public void run() {

        // 检查是否有等待回复的消息超时
        WTFSocketSessionFactory.SERVER.checkResponseMsgTimeout();

        if (WTFSocketSessionFactory.SERVER.hasWaitSendMsg()) {
            doWrite(WTFSocketSessionFactory.SERVER);
        }

        for (WTFSocketSession session : WTFSocketSessionFactory.getSessions()) {

            // 服务器会话已优先处理
            if (ObjectUtils.equals(session, WTFSocketSessionFactory.SERVER)) {
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

        Socket socket = WTFSocketBootstrap.getSocket();

        WTFSocketMsgWrapper msgWrapper = session.nextWaitSendMsg();
        try {

            while (msgWrapper != null) {

                if (!socket.isClosed() && socket.isConnected()) {
                    if (msgWrapper.getMsgType() != 0) {
                        WTFSocketLogUtils.info(String.format(
                                "send msg from <%s> to <%s>:\nmsg => %s",
                                msgWrapper.getFrom(),
                                msgWrapper.getTo(),
                                msgWrapper
                        ));
                    }

                    byte[] bytes = WTFSocketSessionFactory.getEncoder().encode(msgWrapper + WTFSocketProtocolParser.EOT);
                    socket.getOutputStream().write(bytes);

                }else{
                    session.rollbackSendMsg(msgWrapper);
                }

                msgWrapper = session.nextWaitSendMsg();
            }

        } catch (IOException e) {
            WTFSocketSessionFactory.dispatchException(new WTFSocketIOException(e.getMessage()), msgWrapper);
        }
    }
}
