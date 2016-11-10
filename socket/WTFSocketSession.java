package wtf.socket;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * socket会话
 * 每个会话均只有唯一会话对象
 * 会话对象一旦设置则不允许更改
 * 会话对象只能通过SocketSessionFactory创建
 */
public class WTFSocketSession {

    private static final WTFSocketHandler DEFAULT_RESPONSE = new WTFSocketHandler() {
    };

    // 自身名称
    private String from;

    // 会话名称
    private String to;

    // 默认响应方法
    private WTFSocketHandler defaultResponse = DEFAULT_RESPONSE;

    // 等待回复消息队列
    private ConcurrentLinkedQueue<WTFSocketMsgWrapper> waitResponseMsgQ = new ConcurrentLinkedQueue<>();

    // 等待发送信息队列
    private ConcurrentLinkedQueue<WTFSocketMsgWrapper> waitSendMsgQ = new ConcurrentLinkedQueue<>();

    // 回滚信息队列
    private ConcurrentLinkedQueue<WTFSocketMsgWrapper> rollbackMsgQ = new ConcurrentLinkedQueue<>();

    // 屏蔽构造函数
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
     * @param msg     消息对象
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

        WTFSocketMsgWrapper msgWrapper = WTFSocketMsgWrapper.wrapMsg(this, msg);
        msgWrapper.setTimeout(System.currentTimeMillis() + timeout);

        if (handler != null) {
            msgWrapper.setHandler(handler);
            msgWrapper.setNeedResponse(true);
        }

        waitSendMsgQ.add(msgWrapper);
    }

    /**
     * 回复某条消息
     * 回复消息不会产生新的msgId
     *
     * @param reply    回复的消息
     * @param original 原始消息
     */
    public void replyMsg(WTFSocketMsg reply, WTFSocketMsg original) {
        WTFSocketMsgWrapper msgWrapper = WTFSocketMsgWrapper.wrapMsg(this, reply).setMsgId(original.getMsgId());
        waitSendMsgQ.add(msgWrapper);
    }

    /**
     * 取消发送某条消息
     * 如果消息已被发送，则取消无效
     *
     * @param msg 需要撤回的消息
     */
    public void cancelMsg(WTFSocketMsg msg) {

        WTFSocketMsgWrapper msgWrapper = msg.getWrapper();

        if (waitSendMsgQ.contains(msgWrapper)) {
            waitSendMsgQ.remove(msgWrapper);
            return;
        }

        if (waitResponseMsgQ.contains(msgWrapper)) {
            waitResponseMsgQ.remove(msgWrapper);
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
        defaultResponse = DEFAULT_RESPONSE;
    }

    /**
     * 关闭会话
     */
    public void close() {
        if (ObjectUtils.equals(this, WTFSocketSessionFactory.HEARTBEAT)) {
            this.clearWaitQ(waitSendMsgQ, false);
            this.clearWaitQ(waitResponseMsgQ, false);
        } else {
            this.clearWaitQ(waitSendMsgQ, true);
            this.clearWaitQ(waitResponseMsgQ, true);
        }
        WTFSocketSessionFactory.unRegisterSession(this);
    }

    // 派发消息
    boolean dispatchMsg(WTFSocketMsgWrapper msgWrapper) {

        String msgTag = msgWrapper.getTag();
        int len = waitResponseMsgQ.size();

        for (int i = 0; i < len; i++) {
            WTFSocketMsgWrapper wrapper = waitResponseMsgQ.poll();
            if (StringUtils.equals(wrapper.getTag(), msgTag)) {
                if (wrapper.getHandler().onReceive(this, msgWrapper.getMsg())) {
                    return true;
                }
            } else {
                waitResponseMsgQ.add(wrapper);
            }
        }

        return defaultResponse.onReceive(this, msgWrapper.getMsg());

    }

    // 派发异常
    boolean dispatchException(WTFSocketException e, WTFSocketMsgWrapper msgWrapper) {

        if (msgWrapper.getHandler().onException(this, msgWrapper.getMsg(), e)) {
            return true;
        }

        return defaultResponse.onException(msgWrapper.getBelong(), msgWrapper.getMsg(), e);

    }

    // 检查是否有等待回复超时
    void checkResponseMsgTimeout() {

        // 如果是心跳包丢失引起的异常
        // 会在dispatchException的过程中清空等待回复队列
        // 使用 for( : ) 语句会出问题，这是个坑
        // 这样提醒我如果遍历过程中有可能修改 集合对象 的操作
        // 尽量不要使用 for( : ) 语句
        for (int i = 0; i < waitResponseMsgQ.size(); i++) {
            WTFSocketMsgWrapper wrapper = waitResponseMsgQ.poll();

            if (wrapper.isTimeout()) {
                WTFSocketSessionFactory.dispatchException(
                        new WTFSocketTimeoutException("wait response timed out"),
                        wrapper);
            } else {
                waitResponseMsgQ.add(wrapper);
            }
        }
    }

    // 判断是否有消息等待发送
    boolean hasWaitSendMsg() {
        return !waitSendMsgQ.isEmpty();
    }

    // 获取下一个等待发送的消息
    // 消息一旦获取将会被移除等待发送队列
    // 如果没有消息了则返回空
    WTFSocketMsgWrapper nextWaitSendMsg() {

        if (hasWaitSendMsg()) {

            while (!waitSendMsgQ.isEmpty()) {

                WTFSocketMsgWrapper msgWrapper = waitSendMsgQ.poll();
                if (msgWrapper.isTimeout()) {
                    WTFSocketSessionFactory.dispatchException(new WTFSocketTimeoutException("wait send timed out"), msgWrapper);
                    continue;
                }
                if (msgWrapper.isNeedResponse()) {
                    waitResponseMsgQ.add(msgWrapper);
                }
                return msgWrapper;
            }

        }
        while (!rollbackMsgQ.isEmpty()) {
            waitSendMsgQ.add(rollbackMsgQ.poll());
        }
        return null;
    }

    // 回滚一条待发送消息
    void rollbackSendMsg(WTFSocketMsgWrapper msgWrapper) {
        if (msgWrapper.isNeedResponse()) {
            waitResponseMsgQ.remove(msgWrapper);
        }
        rollbackMsgQ.add(msgWrapper);
    }

    // 清空等待回复队列
    void clearWaitQ(ConcurrentLinkedQueue<WTFSocketMsgWrapper> waitQ, boolean isTrigger) {
        if (isTrigger) {
            while (!waitQ.isEmpty()) {
                WTFSocketSessionFactory.dispatchException(
                        new WTFSocketTimeoutException("wait response timed out"),
                        waitQ.poll());
            }
        } else {
            waitQ.clear();
        }
    }

    // 清空 id < msgId 的消息的等待
    // 因为 msgId 是时间递增的
    // 所有可用认为是清空某天消息之前的消息
    void removeWaitResponseMsgBefore(WTFSocketMsg msg) {

        int len = waitResponseMsgQ.size();

        for (int i = 0; i < len; i++) {
            WTFSocketMsgWrapper wrapper = waitResponseMsgQ.poll();
            if (Integer.valueOf(wrapper.getTag()) > msg.getMsgId()) {
                waitResponseMsgQ.add(wrapper);
            }
        }
    }
}
