package wtf.socket;

import java.util.logging.Logger;

class WTFSocketHeartbeatThread implements Runnable {

    private static Logger logger = Logger.getLogger("socket");
    private int breakTime;

    WTFSocketHeartbeatThread(int breakTime) {
        this.breakTime = breakTime;
    }

    @Override
    public void run() {

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
            }, breakTime);
        }
}
