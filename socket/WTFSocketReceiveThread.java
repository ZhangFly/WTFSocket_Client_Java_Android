package socket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;

import java.io.IOException;
import java.net.Socket;

class WTFSocketReceiveThread implements Runnable {


    private WTFSocketBootstrap wtfSocketClient;

    WTFSocketReceiveThread(WTFSocketBootstrap wtfSocketClient) {
        this.wtfSocketClient = wtfSocketClient;
    }

    @Override
    public void run() {

        WTFSocketMsgWrapper msgWrapper = null;
        String data = null;

        try {
            Socket socket = wtfSocketClient.getSocket();

            if (socket.isClosed() || !socket.isConnected()) {
                return;
            }

            int toReadLen = socket.getInputStream().available();

            if (toReadLen <= 0) {
                return;
            }

            byte bytes[] = new byte[toReadLen];
            int readLen = socket.getInputStream().read(bytes);
            data = new String(bytes, 0, readLen);

            for (String packet : wtfSocketClient.parseAndGetPackets(data)) {

                msgWrapper = JSON.parseObject(packet, WTFSocketMsgWrapper.class);

                if (msgWrapper.getFrom() == null ) {
                    throw new IOException("protocol err => lack <from>");
                }

                if (msgWrapper.getTo() == null) {
                    throw new IOException("protocol err => lack <to>");
                }

                if (msgWrapper.getMsgId() == null) {
                    throw new IOException("protocol err => lack <msgId>");
                }

                if (msgWrapper.getMsgType() == null) {
                    throw new IOException("protocol err => lack <msgType>");
                }

                if (msgWrapper.getMsgType() == 1) {
                    WTFSocketLogUtils.info(String.format(
                            "received msg from <%s> to <%s>:\nmsg => %s",
                            msgWrapper.getFrom(),
                            msgWrapper.getTo(),
                            msgWrapper
                    ));
                }
                WTFSocketSessionFactory.dispatchMsg(msgWrapper);
            }

        } catch (IOException | JSONException e) {
            wtfSocketClient.clearBuffer();
            WTFSocketSessionFactory.dispatchException(
                    new WTFSocketException(e.getMessage()).setAddition(data),
                    msgWrapper
            );
        }
    }
}
