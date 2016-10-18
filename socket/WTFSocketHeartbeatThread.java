package wtf.socket;

import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

class WTFSocketHeartbeatThread implements Runnable {

    private static Logger logger = Logger.getLogger("socket");
    private Socket socket;

    @Override
    public void run() {

        while (socket != null && !socket.isClosed()) {

            WTFSocketMsg heartbeatMsg = WTFSocketMsg.heartbeat();
            WTFSocketSessionFactory.HEARTBEAT.sendMsg(heartbeatMsg, new WTFSocketHandler() {

                @Override
                public boolean onReceive(WTFSocketSession session, WTFSocketMsg msg) {

                    session.clearWaitResponsesBefore(msg.getMsgId());

                    return true;
                }

                @Override
                public boolean onException(WTFSocketSession session, WTFSocketMsg msg, WTFSocketException e) {

                    session.clearWaitResponses();

                    WTFSocketSessionFactory.setIsAvailable(false);

                    WTFSocketSessionFactory.deInit();

                    return true;
                }
            }, 15_000);

            try {
                Thread.sleep(5_000);
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, e.getMessage());
            }
        }

        logger.info("socket beat thread stop");
    }

    WTFSocketHeartbeatThread bindSocket(Socket socket) {
        this.socket = socket;
        return this;
    }
}
