package wtf.socket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * socket会话
 * 每个会话均只有唯一会话对象
 * 会话对象一旦设置则不允许更改
 * 会话对象只能通过SocketSessionFactory创建
 */
public class WTFSocketSession {


    // 自身名称
    private String from;

    // 会话名称
    private String to;

    // 默认响应方法
    private WTFSocketHandler defaultResponse = new WTFSocketHandler() {
    };

    // 等待回复消息列表
    private ConcurrentHashMap<String, WTFSocketMsgWrapper> waitResponseMsg = new ConcurrentHashMap<>();

    // 等待发送信息列表
    private ConcurrentHashMap<String, WTFSocketMsgWrapper> waitSendMsg = new ConcurrentHashMap<>();

    WTFSocketSession(String from, String to) {
        this.from = from;
        this.to = to;
    }

    /**
     * 获取会话中的本机地址
     *
     * @return 本机地址
     */
    public String getFrom() {
        return from;
    }

    /**
     * 获取会话中的对方地址
     *
     * @return 对方地址
     */
    public String getTo() {
        return to;
    }


    /**
     * 发送消息给会话对象
     * 会自动添加新的msgId
     * 不接受回复
     *
     * @param msg 消息对象
     *            ·
     */
    public void sendMsg(WTFSocketMsg msg) {

        sendMsg(msg, null);

    }

    /**
     * 发送消息给会话对象
     * 会自动添加新的msgId
     * 不接受回复
     * 在超时后自动自动删除
     *
     * @param msg 消息对象
     * @param timeout 超时时间，单位ms，不能低于500ms
     */
    public void sendMsg(WTFSocketMsg msg, int timeout) {

        sendMsg(msg, null, timeout);

    }

    /**
     * 发送消息给会话对象
     * 会自动添加新的msgId
     * 并通过 handler 对象处理回复
     *
     * @param msg     消息对象
     * @param handler 响应方法
     */
    public void sendMsg(WTFSocketMsg msg, WTFSocketHandler handler) {
        sendMsg(msg, handler, Integer.MAX_VALUE);
    }

    /**
     * 发送消息给会话对象
     * 会自动添加新的msgId
     * 并通过 handler 对象处理回复
     * 在超时后会处罚 handler 处理
     *
     * @param msg     消息对象
     * @param handler 响应方法
     * @param timeout 超时时间，单位ms，不能低于500ms
     */
    public void sendMsg(WTFSocketMsg msg, WTFSocketHandler handler, int timeout) {

        if (timeout < 500) {
            timeout = 500;
        }

        WTFSocketMsgWrapper msgWrapper = new WTFSocketMsgWrapper(this, msg);
        msgWrapper.setTimeout(System.currentTimeMillis() + timeout);

        if (handler != null) {
            msgWrapper.setHandler(handler);
            msgWrapper.setNeedResponse(true);
        }

        waitSendMsg.put(msgWrapper.getTag(), msgWrapper);
    }

    /**
     * 回复某条消息
     * 回复消息不会产生新的msgId
     *
     * @param reply    回复的消息
     * @param original 原始消息
     */
    public void replyMsg(WTFSocketMsg reply, WTFSocketMsg original) {
        WTFSocketMsgWrapper msgWrapper = new WTFSocketMsgWrapper(this, reply).setMsgId(original.getMsgId());
        waitSendMsg.put(msgWrapper.getTag(), msgWrapper);
    }

    /**
     * 取消发送某条消息
     * 如果消息已被发送，则取消无效
     *
     * @param msg 需要撤回的消息
     */
    public void cancelMsg(WTFSocketMsg msg) {

        WTFSocketMsgWrapper msgWrapper = msg.getWrapper();

        if (waitSendMsg.contains(msgWrapper)) {
            waitSendMsg.remove(msgWrapper);
            return;
        }

        if (waitResponseMsg.contains(msgWrapper)) {
            waitResponseMsg.remove(msgWrapper.getTag());
        }
    }

    /**
     * 设置默认响应方法
     *
     * @param defaultResponse 响应方法
     */
    public void setDefaultResponse(WTFSocketHandler defaultResponse) {
        if (defaultResponse != null) {
            this.defaultResponse = defaultResponse;
        }
    }

    /**
     * 移除默认响应方法
     */
    public void removeDefaultResponse() {
        defaultResponse = new WTFSocketHandler() {
        };
    }

    /**
     * 关闭会话
     */
    public void close() {
        WTFSocketSessionFactory.closeSession(this);
    }

    // 派发消息
    boolean dispatchMsg(WTFSocketMsgWrapper msgWrapper) {

        String msgTag = msgWrapper.getTag();

        // 使用单次响应
        if (waitResponseMsg.containsKey(msgTag)) {

            WTFSocketMsgWrapper waitResponseMsgWrapper = waitResponseMsg.get(msgTag);
            waitResponseMsg.remove(msgTag);

            if (waitResponseMsgWrapper.getHandler().onReceive(this, msgWrapper.getMsg())) {
                return true;
            }
        }

        return defaultResponse.onReceive(this, msgWrapper.getMsg());
    }

    // 派发异常
    boolean dispatchException(WTFSocketMsgWrapper msgWrapper, WTFSocketException e) {

        if (msgWrapper.getHandler().onException(this, msgWrapper.getMsg(), e)) {
            return true;
        }

        return defaultResponse.onException(msgWrapper.getBelong(), msgWrapper.getMsg(), e);

    }

    // 获取等待发送消息队列
    ConcurrentHashMap<String, WTFSocketMsgWrapper> getWaitSendMsg() {
        return waitSendMsg;
    }

    // 获取等待回复消息队列
    ConcurrentHashMap<String, WTFSocketMsgWrapper> getWaitResponseMsg() {
        return waitResponseMsg;
    }

    // 清空等待回复消息
    void clearWaitResponseMsg() {
        waitResponseMsg.clear();
    }

    // 清空 id < msgId 的消息的等待
    void clearWaitResponsesBefore(int msgId) {
        for (String key : waitResponseMsg.keySet()) {
            if (Integer.valueOf(key) < msgId) {
                waitResponseMsg.remove(key);
            }
        }
    }

    // 检查是否有发送超时
    void checkSendTimeout() {

        checkTimeout(waitSendMsg);

    }

    // 检查是否有等待回复超时
    void checkResponseTimeout() {

        checkTimeout(waitResponseMsg);

    }

    // 检查是否有消息等待超时
    // 触发齐处理者
    // 并将其移除
    private void checkTimeout(ConcurrentHashMap<String, WTFSocketMsgWrapper> waitMsg) {

        if (!waitMsg.isEmpty()) {

            List<WTFSocketMsgWrapper> timeoutMsg = new ArrayList<>();

            for (WTFSocketMsgWrapper msgWrapper : waitMsg.values()) {
                if (msgWrapper.isTimeout()) {
                    WTFSocketSessionFactory.dispatchException(
                            new WTFSocketException(waitMsg == waitSendMsg ? "wait send timed out" : "wait response timed out"),
                            msgWrapper);
                    timeoutMsg.add(msgWrapper);
                }
            }

            for (WTFSocketMsgWrapper msgWrapper : timeoutMsg) {
                waitMsg.remove(msgWrapper.getTag());
            }
        }

    }

    // 判断是否有消息等待发送
    boolean hasWaitSendMsg() {
        return !waitSendMsg.isEmpty();
    }

}
