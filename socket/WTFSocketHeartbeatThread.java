package wtf.socket;

class WTFSocketHeartbeatThread implements Runnable {

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

                    session.clearWaitResponseMsg();

                    WTFSocketSessionFactory.setIsAvailable(false);

                    WTFSocketSessionFactory.deInit();

                    return true;
                }
            }, breakTime);
        }
}
