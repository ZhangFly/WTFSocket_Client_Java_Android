package wtf.socket;

import com.alibaba.fastjson.JSON;

import java.io.IOException;
import java.net.Socket;
import java.util.Queue;
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

            if (wtfSocketClient.getSocket().isClosed()) {
                return;
            }

            if (WTFSocketSessionFactory.SERVER.hasMsg()) {
                doWrite(WTFSocketSessionFactory.SERVER, msgWrapper);
            }

            for (WTFSocketSession session : WTFSocketSessionFactory.getSessions()) {
                doWrite(session, msgWrapper);
            }

        } catch (IOException e) {
            WTFSocketException exception = new WTFSocketException(e.getMessage());
            exception.setLocation(this.getClass().getName() + "$run");
            exception.setMsg(msgWrapper);

            WTFSocketSessionFactory.dispatchException(exception);
        }

    }


    private void doWrite(WTFSocketSession session, WTFSocketMsgWrapper msg) throws IOException {

        while (session.hasMsg()) {

            msg = session.getMsg();
            String msgStr = JSON.toJSONString(msg);
            if (msg.getMsgType() != 0) {
                logger.info(String.format("sendMsg msg from <%s> to <%s>:\nmsg => %s",
                        msg.getFrom(),
                        msg.getTo(),
                        msgStr));
            }
            wtfSocketClient.getSocket().getOutputStream().write((msgStr + WTFSocketClient.EOT).getBytes());
        }
    }

}
