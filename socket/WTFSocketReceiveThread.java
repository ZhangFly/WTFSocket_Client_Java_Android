package wtf.socket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

class WTFSocketReceiveThread implements Runnable {

    private static final Logger logger = Logger.getLogger("socket");
    private StringBuffer buffer = new StringBuffer();
    private Socket socket;

    @Override
    public void run() {

        logger.info("socket receive thread start");

        while (socket != null && !socket.isClosed()) {

            WTFSocketMsgWrapper msg = new WTFSocketMsgWrapper();

            try {
                if (socket.getInputStream().available() > 0) {

                    byte bytes[] = new byte[2048];
                    int len = socket.getInputStream().read(bytes);
                    msg.setOriginalStr(new String(bytes, 0, len, "UTF-8"));

                    for (String packet : getPackets(msg.getOriginalStr())) {

                        msg = JSON.parseObject(packet, WTFSocketMsgWrapper.class);

                        if (msg.getFrom() == null || msg.getTo() == null || msg.getMsgId() == null || msg.getMsgType() == null ) {
                            throw new IOException("protocol err!");
                        }

                        if (msg.getMsgType() == null || msg.getMsgType() == 1) {
                            logger.info(String.format("received msg from <%s> to <%s>:\nmsg => %s",
                                    msg.getFrom(),
                                    msg.getTo(),
                                    packet));

                        }
                        WTFSocketSessionFactory.dispatchMsg(msg);
                    }
                }
                WTFSocketSessionFactory.checkTimeout();
                Thread.sleep(200);

            } catch (IOException | JSONException | InterruptedException e) {

                buffer.delete(0, buffer.length());

                WTFSocketException exception = new WTFSocketException(e.getMessage());
                exception.setLocation(this.getClass().getName() + "$run");
                exception.setMsg(msg);

                WTFSocketSessionFactory.dispatchException(exception);
            }
        }

        logger.info("socket receive thread stop");

    }

    private List<String> getPackets(String data) throws IOException {

        List<String> packets = new ArrayList<>();
        buffer.append(data);

        while (true) {
            int index = buffer.indexOf(WTFSocketClient.EOT);
            if (index == -1) {
                break;
            }
            String packet = buffer.substring(0, index);
            buffer.delete(0, index + 2);
            packets.add(packet);
        }

        return packets;
    }

    WTFSocketReceiveThread bindSocket(Socket socket) {
        this.socket = socket;
        return this;
    }
}
