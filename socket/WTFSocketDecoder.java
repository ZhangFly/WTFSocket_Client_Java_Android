package wtf.socket;

/**
 * 解码器
 */
public interface WTFSocketDecoder {

    String decode(byte[] data, int len);

}
