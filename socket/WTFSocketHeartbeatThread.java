package wtf.socket;

class WTFSocketHeartbeatThread implements Runnable {

    private int breakTime;

    WTFSocketHeartbeatThread(int breakTime) {
        this.breakTime = breakTime;
    }

    @Override
    public void run() {

        WTFSocketSessionFactory.HEARTBEAT.sendMsg(WTFSocketMsg.heartbeat(), new WTFSocketHandler() {

            @Override
            public boolean onReceive(WTFSocketSession session, WTFSocketMsg msg) {

                session.removeWaitResponseMsgBefore(msg);

                return true;
            }

            @Override
            public boolean onException(WTFSocketSession session, WTFSocketMsg msg, WTFSocketException e) {

                WTFSocketLogUtils.err(
                        String.format(
                                "heartbeat lost :\nmsg => %s\n%s",
                                msg,
                                e
                        )
                );
                WTFSocketSessionFactory.deInit();

                return true;
            }
        }, breakTime);
    }
}
