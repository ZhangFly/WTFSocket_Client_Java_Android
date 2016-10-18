package wtf.socket;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.annotation.JSONField;

class WTFSocketMsgWrapper {

    private String from;
    private String to;
    private Integer msgId;
    private Integer msgType;
    private Integer cmd;
    private Integer flag;
    private Integer errCode;
    private JSONArray params;

    @JSONField(serialize = false)
    private WTFSocketSession belong;
    @JSONField(serialize = false)
    private WTFSocketHandler handler;
    @JSONField(serialize = false)
    private String originalStr;
    @JSONField(serialize = false)
    private Long timeout;


    public WTFSocketMsgWrapper() {
        this(WTFSocketSessionFactory.getSession("Inner"), WTFSocketMsg.empty());
    }

    public WTFSocketMsgWrapper(WTFSocketSession belong, WTFSocketMsg msg) {

        // 每个消息必须有处理对象
        setHandler(new WTFSocketHandler() {});
        // 每个消息必须属于一个 session
        setBelong(belong);
        setMsgId(WTFSocketSessionFactory.getSelfIncrementMsgId());
        setFlag(msg.getFlag());
        setCmd(msg.getCmd());
        setErrCode(msg.getErrCode());
        setParams(msg.getParams());
        setMsgType(msg.getMsgType());
        msg.setWrapper(this);

    }


    public String getFrom() {
        return from;
    }

    public WTFSocketMsgWrapper setFrom(String from) {
        this.from = from;
        return this;
    }

    public String getTo() {
        return to;
    }

    public WTFSocketMsgWrapper setTo(String to) {
        this.to = to;
        return this;
    }

    public Integer getMsgId() {
        return msgId;
    }

    public WTFSocketMsgWrapper setMsgId(Integer msgId) {
        this.msgId = msgId;
        return this;
    }

    public Integer getMsgType() {
        return msgType;
    }

    public WTFSocketMsgWrapper setMsgType(Integer msgType) {
        this.msgType = msgType;
        return this;
    }

    public Integer getCmd() {
        return cmd;
    }

    public WTFSocketMsgWrapper setCmd(Integer cmd) {
        this.cmd = cmd;
        return this;
    }

    public Integer getFlag() {
        return flag;
    }

    public WTFSocketMsgWrapper setFlag(Integer flag) {
        this.flag = flag;
        return this;
    }

    public Integer getErrCode() {
        return errCode;
    }

    public WTFSocketMsgWrapper setErrCode(Integer errCode) {
        this.errCode = errCode;
        return this;
    }

    public JSONArray getParams() {
        return params;
    }

    public WTFSocketMsgWrapper setParams(JSONArray params) {
        this.params = params;
        return this;
    }

    public WTFSocketSession getBelong() {
        return belong;
    }

    public WTFSocketMsgWrapper setBelong(WTFSocketSession belong) {

        setFrom(belong.getFrom());
        setTo(belong.getTo());

        this.belong = belong;
        return this;
    }

    public WTFSocketHandler getHandler() {
        return handler;
    }

    public WTFSocketMsgWrapper setHandler(WTFSocketHandler handler) {
        this.handler = handler;
        return this;
    }

    public Long getTimeout() {
        return timeout;
    }

    public WTFSocketMsgWrapper setTimeout(Long timeout) {
        this.timeout = timeout;
        return this;
    }

    @JSONField(serialize = false)
    public String getTag() {
        return String.valueOf(getMsgId().intValue());
    }

    @JSONField(serialize = false)
    public WTFSocketMsg getMsg() {

        return WTFSocketMsg.empty()
                .setFrom(getFrom())
                .setTo(getTo())
                .setFlag(getFlag())
                .setErrCode(getErrCode())
                .setCmd(getCmd())
                .setMsgId(getMsgId())
                .setMsgType(getMsgType())
                .setParams(getParams())
                .setOriginalStr(getOriginalStr());
    }

    String getOriginalStr() {
        return originalStr;
    }

    WTFSocketMsgWrapper setOriginalStr(String originalStr) {
        this.originalStr = originalStr;
        return this;
    }

}
