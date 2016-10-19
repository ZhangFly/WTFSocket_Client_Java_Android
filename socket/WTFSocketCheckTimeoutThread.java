package wtf.socket;

import java.util.logging.Logger;

class WTFSocketCheckTimeoutThread implements Runnable{

    private static final Logger logger = Logger.getLogger("socket");

    @Override
    public void run() {
        WTFSocketSessionFactory.checkTimeout();
    }
}
