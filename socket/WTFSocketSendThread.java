package wtf.socket;

import com.alibaba.fastjson.JSON;

import java.io.IOException;
import java.net.Socket;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

class WTFSocketSendThread implements Runnable {

    private static final Logger logger = Logger.getLogger("socket");

    private Socket socket;

    @Override
    public void run() {

        logger.info("socket send thread start");

        while (socket != null && !socket.isClosed()) {

            WTFSocketMsgWrapper msg = new WTFSocketMsgWrapper();

            try {

                if (WTFSocketSessionFactory.SERVER.hasMsg()) {
                    doWrite(WTFSocketSessionFactory.SERVER, msg);
                }

                for (WTFSocketSession session : WTFSocketSessionFactory.getSessions()) {

                    if (session.hasMsg()) {
                        doWrite(session, msg);
                    }
                }

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    logger.log(Level.WARNING, e.getMessage());
                }
            } catch (IOException e) {
                WTFSocketException exception = new WTFSocketException(e.getMessage());
                exception.setLocation(this.getClass().getName() + "$run");
                exception.setMsg(msg);

                WTFSocketSessionFactory.dispatchException(exception);
            }
        }

        logger.info("socket send thread stop");
    }

    private void doWrite(WTFSocketSession session, WTFSocketMsgWrapper msg) throws IOException {

        Queue<WTFSocketMsgWrapper> msgQ = session.getWaitSendQ();

        while (msgQ.peek() != null) {

            msg = msgQ.poll();

            String msgStr = JSON.toJSONString(msg);
            if (msg.getMsgType() != null && msg.getMsgType() != 0) {
                logger.info(String.format("sendMsg msg from <%s> to <%s>:\nmsg => %s",
                        msg.getFrom(),
                        msg.getTo(),
                        msgStr));
            }
            socket.getOutputStream().write((msgStr + WTFSocketClient.EOT).getBytes());
        }
    }

    WTFSocketSendThread bindSocket(Socket socket) {
        this.socket = socket;
        return this;
    }
}
