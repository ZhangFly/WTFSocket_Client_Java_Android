package wtf.socket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

/**
 * 协议 V1.0
 */
class WTFSocketMsgWrapper {

    // 消息级别默认handler
    private static final WTFSocketHandler DEFAULT_HANDLER = new WTFSocketHandler() {};

    /* 包装对象 */
    @JSONField(serialize = false)
    private WTFSocketMsg msg;

    /* 辅助属性 */
    @JSONField(serialize = false)
    private WTFSocketSession belong = WTFSocketSessionFactory.EMPTY;

    @JSONField(serialize = false)
    private WTFSocketHandler handler = DEFAULT_HANDLER;

    @JSONField(serialize = false)
    private Long timeout = Long.MAX_VALUE;

    @JSONField(serialize = false)
    private boolean isNeedResponse = false;

    @JSONField(serialize = false)
    private int priority = -1;

    static WTFSocketMsgWrapper empty() {
        return wrapMsg(null, new WTFSocketMsg());
    }

    static WTFSocketMsgWrapper wrapMsg(WTFSocketMsg msg) {
        return wrapMsg(null, msg);
    }

    static WTFSocketMsgWrapper wrapMsg(WTFSocketSession belong, WTFSocketMsg msg) {

        WTFSocketMsgWrapper msgWrapper = new WTFSocketMsgWrapper(belong, msg);
        msg.setMsgId(WTFSocketSessionFactory.getSelfIncrementMsgId());
        return msgWrapper;
    }

    public WTFSocketMsgWrapper() {
        this(null, new WTFSocketMsg());
    }

    public WTFSocketMsgWrapper(WTFSocketSession belong, WTFSocketMsg msg) {

        // msg 不能为空
        if (msg == null) {
            msg = new WTFSocketMsg();
        }

        if (belong != null) {
            this.belong = belong;
            msg.setFrom(belong.getFrom());
            msg.setTo(belong.getTo());
        }

        msg.setWrapper(this);
        this.msg = msg;
    }

    public String getFrom() {
        return msg.getFrom();
    }

    public WTFSocketMsgWrapper setFrom(String from) {
        msg.setFrom(from);
        return this;
    }

    public String getTo() {
        return msg.getTo();
    }

    public WTFSocketMsgWrapper setTo(String to) {
        msg.setTo(to);
        return this;
    }

    public Integer getMsgId() {
        return msg.getMsgId();
    }

    public WTFSocketMsgWrapper setMsgId(Integer msgId) {
        msg.setMsgId(msgId);
        return this;
    }

    public Integer getMsgType() {
        return msg.getMsgType();
    }

    public WTFSocketMsgWrapper setMsgType(Integer msgType) {
        msg.setMsgType(msgType);
        return this;
    }

    public String getVersion() {
        return msg.getVersion();
    }

    public void setVersion(String version) {
        msg.setVersion(version);
    }

    public Integer getState() {
        return msg.getState();
    }

    public void setState(Integer state) {
        msg.setState(state);
    }

    public JSONObject getBody() {
        return msg.getBody();
    }

    public void setBody(JSONObject body) {
        msg.setBody(body);
    }

    @JSONField(serialize = false)
    public WTFSocketSession getBelong() {
        return belong;
    }

    @JSONField(serialize = false)
    public WTFSocketMsgWrapper setBelong(WTFSocketSession belong) {

        setFrom(belong.getFrom());
        setTo(belong.getTo());

        this.belong = belong;
        return this;
    }

    @JSONField(serialize = false)
    public WTFSocketHandler getHandler() {
        return handler;
    }

    @JSONField(serialize = false)
    public WTFSocketMsgWrapper setHandler(WTFSocketHandler handler) {
        this.handler = handler;
        return this;
    }

    @JSONField(serialize = false)
    public Long getTimeout() {
        return timeout;
    }

    @JSONField(serialize = false)
    public WTFSocketMsgWrapper setTimeout(Long timeout) {
        this.timeout = timeout;
        return this;
    }

    @JSONField(serialize = false)
    public WTFSocketMsg getMsg() {

        return msg;
    }

    @JSONField(serialize = false)
    public boolean isNeedResponse() {
        return isNeedResponse;
    }

    @JSONField(serialize = false)
    public void setNeedResponse(boolean needResponse) {
        isNeedResponse = needResponse;
    }

    @JSONField(serialize = false)
    public String getTag() {
        return String.valueOf(getMsgId().intValue());
    }

    @JSONField(serialize = false)
    public boolean isTimeout() {
        return timeout < System.currentTimeMillis();
    }

    @JSONField(serialize = false)
    public int getPriority() {
        return priority;
    }

    @JSONField(serialize = false)
    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

}
