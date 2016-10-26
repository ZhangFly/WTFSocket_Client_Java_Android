package wtf.socket;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

class WTFSocketReceiveTask implements Runnable {

    @Override
    public void run() {

        Socket socket = WTFSocketBootstrap.getSocket();
        List<WTFSocketMsgWrapper> packets = new ArrayList<>();
        WTFSocketProtocolParser parser =  WTFSocketBootstrap.getParser();
        String data = null;

        try {

            if (!parser.isEmpty()) {
                parser.parseAndLoadPackets(null, packets, WTFSocketMsgWrapper.class);
            }

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

            parser.parseAndLoadPackets(data, packets, WTFSocketMsgWrapper.class);

        } catch (Exception e) {
            WTFSocketSessionFactory.dispatchException(
                    new WTFSocketException(e.getMessage()).setAddition(data)
            );
        } finally {
            for (WTFSocketMsgWrapper packet : packets) {

                if (packet.getMsgType() == 1) {
                    WTFSocketLogUtils.info(String.format(
                            "received msg from <%s> to <%s>:\nmsg => %s",
                            packet.getFrom(),
                            packet.getTo(),
                            packet
                    ));
                }
                WTFSocketSessionFactory.dispatchMsg(packet);
            }
        }
    }
}
