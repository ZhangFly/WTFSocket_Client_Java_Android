package wtf.socket;

class WTFSocketHeartbeatTask implements Runnable {

    @Override
    public void run() {

        WTFSocketConfig config = WTFSocketSessionFactory.getConfig();

        WTFSocketMsg heartbeat = new WTFSocketMsg();

        heartbeat.setMsgType(0);

        WTFSocketSessionFactory.HEARTBEAT.sendMsg(heartbeat, new WTFSocketHandler() {

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
        }, config.getHeartbeatBreakTime() * config.getHeartbeatPeriod());
    }
}
