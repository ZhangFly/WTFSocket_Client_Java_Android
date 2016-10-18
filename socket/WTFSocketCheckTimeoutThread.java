package wtf.socket;

class WTFSocketCheckTimeoutThread implements Runnable{
    @Override
    public void run() {
        WTFSocketSessionFactory.checkTimeout();
    }
}
