package wtf.socket;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * socket会话工厂
 * 负责创建会话和分发消息
 * 会话工厂初始化后会自动创建一个连接到服务器的会话 SERVER
 */
public class WTFSocketSessionFactory {

    // 本机配置
    private static WTFSocketConfig config = null;

    // 服务器会话
    public static WTFSocketSession SERVER = null;

    // 心跳包会话
    static WTFSocketSession HEARTBEAT = null;

    // 空会话
    // 所有无法定位会话对象的消息/异常会被分配到该会话下
    static WTFSocketSession EMPTY = null;

    // 会话表
    private static ConcurrentHashMap<String, WTFSocketSession> sessions = new ConcurrentHashMap<>();

    // 状态标志
    private static volatile boolean isAvailable = false;

    // socket 客服端
    private static WTFSocketBootstrap socketClient = null;

    // 自增的消息ID
    private static AtomicInteger msgId = new AtomicInteger(0);

    // 事件监听者
    private static List<WTFSocketEventListener> eventListeners = new ArrayList<>();

    // 默认响应方法
    private static WTFSocketHandler defaultResponse = new WTFSocketHandler() {
    };

    // 打印
    private static WTFSocketHandler printHandler = new WTFSocketHandler() {
        @Override
        public boolean onReceive(WTFSocketSession session, WTFSocketMsg msg) {
            WTFSocketLogUtils.info(String.format(
                    "printHandler: receive msg from <%s> to <%s>:\r\nmsg => %s",
                    session.getFrom(),
                    session.getTo(),
                    msg
            ));
            return true;
        }

        @Override
        public boolean onException(WTFSocketSession session, WTFSocketMsg msg, WTFSocketException e) {
            WTFSocketLogUtils.err(String.format(
                    "printHandler: occur exception from <%s> to <%s>:\r\nmsg => %s\r\n%s",
                    session.getFrom(),
                    session.getTo(),
                    msg,
                    e
            ));
            return true;
        }
    };

    /**
     * 初始化工厂
     *
     * @param config 网络连接参数
     */
    public static void init(WTFSocketConfig config) {

        if (config == null) {
            WTFSocketLogUtils.err("config can not be null!");
            return;
        }

        if (config.getLocalName() == null) {
            WTFSocketLogUtils.err("config.localName can not be null!");
            return;
        }

        if (config.getIp() == null) {
            WTFSocketLogUtils.err("config.ip can not be null!");
            return;
        }

        if (config.getPort() == 0) {
            WTFSocketLogUtils.err("config.port can not be 0!");
            return;
        }

        WTFSocketSessionFactory.config = config;

        EMPTY = getSession("empty");
        SERVER = getSession("server");
        HEARTBEAT = getSession("heartbeat");

        if (socketClient == null) {
            socketClient = new WTFSocketBootstrap(config);
        }
        socketClient.start();
    }

    /**
     * 反初始化
     * 不会关闭监听线程
     *
     */
    public static void deInit() {

        if (isAvailable) {
            // 关闭socket客户端
            socketClient.close();
            // 设置状态到不可用
            isAvailable = false;
        }
        for (WTFSocketSession session : sessions.values()) {
            session.close();
        }
        notifyEventListeners(WTFSocketEventType.DISCONNECT);
    }

    /**
     * 复位
     *
     */
    public static void reInit() {
        if (isAvailable) {
            deInit();
        }
        init(config);
    }

    /**
     * 终止框架
     * 并关闭监听线程
     *
     */
    public static void shutdown() {
        socketClient.shutdown();
    }

    /**
     * 获取的会话
     * 如果会话不存在会自动创建会话
     *
     * @param to 目标地址
     * @return wtf.WTFSocketSession
     */
    public static WTFSocketSession getSession(String to) {

        return getSession(to, WTFSocketMsg.empty());
    }

    /**
     * 关闭会话
     *
     * @param session 会话对象
     */
    public static void closeSession(WTFSocketSession session) {
        if (session != null) {
            session.close();
        }
    }

    /**
     * 添加默认响应方法
     * 与session默认响应方法不同的是这里返回的是 session 对象而不是 msg 对象
     *
     * @param defaultResponse 回调方法
     */
    public static void setDefaultResponse(WTFSocketHandler defaultResponse) {
        if (defaultResponse != null) {
            WTFSocketSessionFactory.defaultResponse = defaultResponse;
        }
    }

    /**
     * 移除默认响应方法
     */
    public static void removeDefaultResponse() {
        defaultResponse = new WTFSocketHandler() {
        };
    }


    /**
     * 添加事件监听者
     *
     * @param listener 事件监听者
     */
    public static void addEventListener(WTFSocketEventListener listener) {
        if (!eventListeners.contains(listener)) {
            eventListeners.add(listener);
        }
    }

    /**
     * 移除事件监听者
     *
     * @param listener 事件监听者
     */
    public static void removeEventListener(WTFSocketEventListener listener) {
        if (eventListeners.contains(listener)) {
            eventListeners.remove(listener);
        }
    }

    /**
     * 清空所有事件监听者
     *
     * @param listener 事件监听者
     */
    public static void clearEventListeners(WTFSocketEventListener listener) {
        eventListeners.clear();
    }

    /**
     * 框架是否就绪
     *
     * @return true 可用，false 不可用
     */
    public static boolean isAvailable() {
        return isAvailable;
    }

    // 获取会话对象内部方法
    static WTFSocketSession getSession(String to, WTFSocketMsg msg) {

        if (sessions.containsKey(to)) {
            return sessions.get(to);
        }

        WTFSocketSession session = new WTFSocketSession(config.getLocalName(), to);
        sessions.put(to, session);
        if (!"server".equals(to) && !"empty".equals(to) && !"heartbeat".equals(to))

        if (!StringUtils.equals("server", to)
                && StringUtils.equals("empty", to)
                && StringUtils.equals("heartbeat", to)) {
            notifyEventListeners(WTFSocketEventType.NEW_SESSION, session, msg);
        }
        return session;
    }

    // 设置框架就绪
    static void setIsAvailable(boolean isAvailable) {
        WTFSocketSessionFactory.isAvailable = isAvailable;
    }

    // 获取所有 session
    static Collection<WTFSocketSession> getSessions() {
        return sessions.values();
    }

    // 获取自增的消息ID
    static int getSelfIncrementMsgId() {
        return msgId.getAndAdd(1);
    }

    // 消息派发
    static boolean dispatchMsg(WTFSocketMsgWrapper msgWrapper) {

        // 处理心跳包
        if (msgWrapper.getMsgType() == 0) {
            return HEARTBEAT.dispatchMsg(msgWrapper);
        }

        // 处理普通消息
        WTFSocketSession session = getSession(msgWrapper.getFrom(), msgWrapper.getMsg());

        if (!session.dispatchMsg(msgWrapper)) {
            if (!defaultResponse.onReceive(session, msgWrapper.getMsg())) {
                return printHandler.onReceive(session, msgWrapper.getMsg());
            }
        }

        return true;
    }

    // 异常派发
    static boolean dispatchException(WTFSocketException e) {
        return dispatchException(e, null);
    }

    // 异常派发
    static boolean dispatchException(WTFSocketException e, WTFSocketMsgWrapper msgWrapper) {

        if (msgWrapper == null) {
            msgWrapper = WTFSocketMsgWrapper.empty();
        }

        WTFSocketSession session = msgWrapper.getBelong();

        if (!session.dispatchException(e, msgWrapper)) {
            // 会话对象无法处理消息
            // 使用默认响应函数
            if (!defaultResponse.onException(msgWrapper.getBelong(), msgWrapper.getMsg(), e)) {
                return printHandler.onException(session, msgWrapper.getMsg(), e);
            }

        }

        return true;
    }

    // 从注册表中将 session 移除
    static void unRegisterSession(WTFSocketSession session) {
        if (sessions.contains(session)) {
            sessions.remove(session.getTo());
        }
    }

    // 通知监听者
    static void notifyEventListeners(WTFSocketEventType type, Object... params) {

        for (WTFSocketEventListener listener : eventListeners) {

            switch (type) {
                case CONNECT:
                    listener.onConnect();
                    break;
                case DISCONNECT:
                    listener.onDisconnect();
                    break;
                case NEW_SESSION:
                    listener.onNewSession((WTFSocketSession) params[0], (WTFSocketMsg) params[1]);
                    break;
                default:
                    break;
            }
        }
    }
}
