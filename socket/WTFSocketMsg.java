package wtf.socket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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

    @Option
    private Integer flag;
    @Option
    private Integer errCode;
    @Option
    private Integer cmd;
    @Option
    private JSONArray params;


    @JSONField(serialize = false)
    private WTFSocketMsgWrapper wrapper;

    /* 屏蔽构造函数 */
    private WTFSocketMsg() {

    }

    /**
     * 创建空消息模板
     *
     * @return 消息模板
     */
    public static WTFSocketMsg empty() {
        return new WTFSocketMsg();
    }

    /**
     * 创建成功消息模板
     *
     * @return 消息模板
     */
    public static WTFSocketMsg success() {
        return new WTFSocketMsg().setFlag(1);
    }

    /**
     * 创建失败消息模板
     *
     * @return 消息模板
     */
    public static WTFSocketMsg failure(int errCode) {
        return new WTFSocketMsg().setFlag(0).setErrCode(errCode);
    }

    /**
     * 创建心跳包消息模板
     *
     * @return 消息模板
     */
    public static WTFSocketMsg heartbeat() {
        return new WTFSocketMsg().setMsgType(0);
    }

    /**
     * 获取cmd属性
     *
     * @return cmd值
     */
    public Integer getCmd() {
        return cmd;
    }

    /**
     * 设置cmd属性
     *
     * @return this
     */
    public WTFSocketMsg setCmd(Integer cmd) {
        this.cmd = cmd;
        return this;
    }

    /**
     * 获取params属性
     *
     * @return cmd值
     */
    public JSONArray getParams() {
        return params;
    }

    /**
     * 设置params属性
     *
     * @return this
     */
    public WTFSocketMsg setParams(JSONArray params) {
        this.params = params;
        return this;
    }

    /**
     * 添加一个对象到params属性
     *
     * @return this
     */
    public WTFSocketMsg addParam(Object param) {
        if (params == null) {
            params = new JSONArray();
        }
        params.add(param);
        return this;
    }

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

    WTFSocketMsgWrapper getWrapper() {
        return wrapper;
    }

    WTFSocketMsg setWrapper(WTFSocketMsgWrapper wrapper) {
        this.wrapper = wrapper;
        return this;
    }
}
