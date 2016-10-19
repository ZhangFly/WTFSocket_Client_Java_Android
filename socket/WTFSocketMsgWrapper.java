package wtf.socket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.annotation.JSONField;
import com.sun.istack.internal.Nullable;
import com.sun.javafx.beans.annotations.NonNull;

class WTFSocketMsgWrapper {

    /* 包装对象 */
    @NonNull
    @JSONField(serialize = false)
    private WTFSocketMsg msg;

    /* 辅助属性 */
    @NonNull
    @JSONField(serialize = false)
    private WTFSocketSession belong;

    @NonNull
    @JSONField(serialize = false)
    private WTFSocketHandler handler;

    @Nullable
    @JSONField(serialize = false)
    private Long timeout;

    @NonNull
    @JSONField(serialize = false)
    private boolean isNeedResponse = false;

    public WTFSocketMsgWrapper() {
        this(null, null);
    }

    public WTFSocketMsgWrapper(WTFSocketSession belong, WTFSocketMsg msg) {

        // msg 不能为空
        if (msg == null) {
            msg = WTFSocketMsg.empty();
        }

        // belong 不能为空
        if (belong == null) {
            belong = WTFSocketSessionFactory.getSession("Inner");
        }

        msg.setFrom(belong.getFrom());
        msg.setTo(belong.getTo());
        msg.setMsgId(WTFSocketSessionFactory.getSelfIncrementMsgId());
        msg.setWrapper(this);

        this.msg = msg;
        this.belong = belong;

        // 每个消息必须有处理对象
        this.handler = new WTFSocketHandler() {};
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

    public Integer getCmd() {
        return msg.getCmd();
    }

    public WTFSocketMsgWrapper setCmd(Integer cmd) {
        msg.setCmd(cmd);
        return this;
    }

    public Integer getFlag() {
        return msg.getFlag();
    }

    public WTFSocketMsgWrapper setFlag(Integer flag) {
        msg.setFlag(flag);
        return this;
    }

    public Integer getErrCode() {
        return msg.getErrCode();
    }

    public WTFSocketMsgWrapper setErrCode(Integer errCode) {
        msg.setErrCode(errCode);
        return this;
    }

    public JSONArray getParams() {
        return msg.getParams();
    }

    public WTFSocketMsgWrapper setParams(JSONArray params) {
        msg.setParams(params);
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

    public WTFSocketMsg getMsg() {

        return msg;
    }

    public boolean isNeedResponse() {
        return isNeedResponse;
    }

    public void setNeedResponse(boolean needResponse) {
        isNeedResponse = needResponse;
    }

    public String getOriginalStr() {
        return msg.getOriginalStr();
    }

    public WTFSocketMsgWrapper setOriginalStr(String originalStr) {
        msg.setOriginalStr(originalStr);
        return this;
    }

    @JSONField(serialize = false)
    public String getTag() {
        return String.valueOf(getMsgId().intValue());
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
