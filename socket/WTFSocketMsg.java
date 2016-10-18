package wtf.socket;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.annotation.JSONField;

/**
 * Socket通信消息模板
 * from/to属性为框架自动填充
 * 个人设置将会被忽略
 */
public class WTFSocketMsg {

    private String from;
    private String to;
    @JSONField(serialize = false)
    private Integer msgId;
    @JSONField(serialize = false)
    private Integer msgType;
    private Integer flag;
    private Integer errCode;
    private Integer cmd;
    private JSONArray params;
    @JSONField(serialize = false)
    private String originalStr;

    @JSONField(serialize = false)
    private WTFSocketMsgWrapper wrapper;

    public static WTFSocketMsg empty() {
        WTFSocketMsg template = new WTFSocketMsg();
        template.setMsgType(1);
        return template;
    }


    public static WTFSocketMsg success() {
        WTFSocketMsg template = new WTFSocketMsg();
        template.setFlag(1);
        template.setMsgType(1);
        return template;
    }

    public static WTFSocketMsg failure(int errCode) {
        WTFSocketMsg template = new WTFSocketMsg();
        template.setFlag(0);
        template.setErrCode(errCode);
        template.setMsgType(1);
        return template;
    }

    public static WTFSocketMsg heartbeat() {
        WTFSocketMsg template = new WTFSocketMsg();
        template.setMsgType(0);
        return template;
    }

    private WTFSocketMsg() {

    }

    public Integer getCmd() {
        return cmd;
    }

    public WTFSocketMsg setCmd(Integer cmd) {
        this.cmd = cmd;
        return this;
    }


    public JSONArray getParams() {
        return params;
    }

    public WTFSocketMsg setParams(JSONArray params) {
        this.params = params;
        return this;
    }

    public WTFSocketMsg addParam(Object param) {
        if (params == null) {
            params = new JSONArray();
        }
        params.add(param);
        return this;
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

    public Integer getFlag() {
        return flag;
    }

    WTFSocketMsg setFlag(Integer flag) {
        this.flag = flag;
        return this;
    }

    public Integer getErrCode() {
        return errCode;
    }

    WTFSocketMsg setErrCode(Integer errCode) {
        this.errCode = errCode;
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

    String getOriginalStr() {
        return originalStr;
    }

    WTFSocketMsg setOriginalStr(String originalStr) {
        this.originalStr = originalStr;
        return this;
    }

    WTFSocketMsgWrapper getWrapper() {
        return wrapper;
    }

    WTFSocketMsg setWrapper(WTFSocketMsgWrapper wrapper) {
        this.wrapper = wrapper;
        return this;
    }
}
