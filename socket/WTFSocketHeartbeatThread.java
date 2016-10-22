package socket;

class WTFSocketHeartbeatThread implements Runnable {

    private int breakTime;

    WTFSocketHeartbeatThread(int breakTime) {
        this.breakTime = breakTime;
    }

    @Override
    public void run() {

        if (!WTFSocketSessionFactory.isAvailable()) {
            return;
        }

        WTFSocketMsg heartbeatMsg = WTFSocketMsg.heartbeat();
        WTFSocketSessionFactory.HEARTBEAT.sendMsg(heartbeatMsg, new WTFSocketHandler() {

            @Override
            public boolean onReceive(WTFSocketSession session, WTFSocketMsg msg) {

                session.removeWaitResponseMsgBefore(msg.getMsgId());

                return true;
            }

            @Override
            public boolean onException(WTFSocketSession session, WTFSocketMsg msg, WTFSocketException e) {

                WTFSocketSessionFactory.deInit();

                return true;
            }
        }, breakTime);
    }
}
