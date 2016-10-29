package wtf.socket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import wtf.socket.WTFSocketAnnotations.Necessary;
import wtf.socket.WTFSocketAnnotations.Option;

/**
 * Socket通信消息模板
 * from/to属性为框架自动填充
 * 个人设置将会被忽略
 *
 * 协议 V1.0
 */
public class WTFSocketMsg {

    @Necessary
    private String from = "unknown";
    @Necessary
    private String to = "unknown";
    @Necessary
    private Integer msgId = 0;
    @Necessary
    private Integer msgType = 1;
    @Necessary
    private Integer state = 1;
    @Necessary
    private String version = "2.0";

    @Option
    private JSONObject body = null;



    @JSONField(serialize = false)
    private WTFSocketMsgWrapper wrapper;

    @Override
    public String toString() {
        return JSON.toJSONString(wrapper);
    }

    String getFrom() {
        return from;
    }

    WTFSocketMsg setFrom(String from) {
        this.from = from;
        return this;
    }

    String getTo() {
        return to;
    }

    WTFSocketMsg setTo(String to) {
        this.to = to;
        return this;
    }

    Integer getMsgId() {
        return msgId;
    }

    WTFSocketMsg setMsgId(Integer msgId) {
        this.msgId = msgId;
        return this;
    }

    Integer getMsgType() {
        return msgType;
    }

    WTFSocketMsg setMsgType(Integer msgType) {
        this.msgType = msgType;
        return this;
    }

    WTFSocketMsgWrapper getWrapper() {
        return wrapper;
    }

    WTFSocketMsg setWrapper(WTFSocketMsgWrapper wrapper) {
        this.wrapper = wrapper;
        return this;
    }

    public Integer getState() {
        return state;
    }

    void setState(Integer state) {
        this.state = state;
    }

    String getVersion() {
        return version;
    }

    void setVersion(String version) {
        this.version = version;
    }

    public JSONObject getBody() {
        return body;
    }

    public WTFSocketMsg setBody(JSONObject body) {
        this.body = body;
        return this;
    }
}
