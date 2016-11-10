package wtf.socket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import wtf.socket.WTFSocketAnnotations.Necessary;
import wtf.socket.WTFSocketAnnotations.Option;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 协议解析器
 */
class WTFSocketProtocolParser {

    // 传输终止符
    static final String EOT = "\r\n";

    // 缓冲器
    private StringBuffer buffer = new StringBuffer();

    // 协议必须属性检查列表
    private List<String> necessaryAttrs = new ArrayList<>();

    // 协议可选属性检查列表
    private List<String> optionAttrs = new ArrayList<>();

    // 初始化
    // 会加载必要参数检查列表
    WTFSocketProtocolParser(Class<?> template) {

        Field[] fields = template.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Option.class)) {
                optionAttrs.add(field.getName());
            }
            if (field.isAnnotationPresent(Necessary.class)) {
                necessaryAttrs.add(field.getName());
            }
        }
    }

    // 检查协议完整性
    // 并转换为JSONObject
    private JSONObject checkAndConvert(String packet) throws WTFSocketLackNecessaryAttrException, WTFSocketProtocolFormatException {

        JSONObject json;

        try {
            json = JSON.parseObject(packet);
        } catch (JSONException e) {
            WTFSocketProtocolFormatException e1 = new WTFSocketProtocolFormatException(e.getMessage());
            e1.setAddition(packet);
            throw e1;
        }
        for (String attr : necessaryAttrs) {
            if (!json.containsKey(attr)) {
                throw new WTFSocketLackNecessaryAttrException("lack necessary attr => <" + attr + ">");
            }
        }
        return json;
    }

    // 解析并装载有效数据包
    // 数据包将会被装载到 packets 中
    // 如果方式异常将会中断
    <T> void parseAndLoadPackets(String data, List<T> packets, Class<T> tClass) throws WTFSocketLackNecessaryAttrException, WTFSocketProtocolFormatException {
        if (data != null) {
            buffer.append(data);
        }
        while (true) {
            int index = buffer.indexOf(EOT);
            if (index == -1) {
                break;
            }
            String packet = buffer.substring(0, index);
            buffer.delete(0, index + 2);
            JSONObject json = checkAndConvert(packet);
            packets.add(json.toJavaObject(tClass));
        }
    }

    // 解释器是否为空
    // 连续数据中有一个错误的数据包时
    // 解释器会抛出异常，并将剩下的数据缓存
    boolean isEmpty() {
        return buffer.length() > 0;
    }
}
