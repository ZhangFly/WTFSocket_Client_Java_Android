package wtf.socket;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.*;

/**
 * socket会话工厂
 * 负责创建会话和分发消息
 * 会话工厂初始化后会自动创建一个连接到服务器的会话  SERVER
 */
public class WTFSocketSessionFactory {

    private static Logger logger = Logger.getLogger("socket");

    // 本机配置
    private static WTFSocketConfig config;

    // 服务器会话
    public static WTFSocketSession SERVER;

    // 心跳包会话
    static WTFSocketSession HEARTBEAT;

    // 空会话
    // 所有无法定位会话对象的消息/异常会被分配到该会话下
    static WTFSocketSession EMPTY;

    private static ConcurrentHashMap<String, WTFSocketSession> sessions = new ConcurrentHashMap<>();

    private static volatile boolean isAvailable = false;

    // socket 客服端
    private static WTFSocketBootstrap socketClient;

    // 自增的消息ID
    private static AtomicInteger msgId = new AtomicInteger(0);

    // 事件监听者
    private static List<WTFSocketEventListener> eventListeners = new ArrayList<>();

    // 默认响应方法
    private static WTFSocketHandler defaultResponse = new WTFSocketHandler() {};

    // 打印响应
    private static WTFSocketHandler printHandler = new WTFSocketHandler() {
        @Override
        public boolean onReceive(WTFSocketSession session, WTFSocketMsg msg) {
            logger.info(String.format("printHandler: receive msg from <%s> to <%s>:\nmsg => %s",
                    session.getFrom(),
                    session.getTo(),
                    JSON.toJSONString(msg)));
            return true;
        }

        @Override
        public boolean onException(WTFSocketSession session, WTFSocketMsg msg, WTFSocketException e) {
            logger.log(Level.WARNING, String.format("printHandler: occur exception from <%s> to <%s>:\noriginalStr => %s\nmsg => %s\n%s",
                    session.getFrom(),
                    session.getTo(),
                    msg.getOriginalStr(),
                    msg,
                    e.getMessage()));
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
            logger.log(Level.WARNING, "config can not be null!");
            return;
        }

        if (config.getLocalName() == null) {
            logger.log(Level.WARNING, "config.localName can not be null!");
            return;
        }

        if (config.getIp() == null) {
            logger.log(Level.WARNING, "config.ip can not be null!");
            return;
        }

        if (config.getPort() == 0) {
            logger.log(Level.WARNING, "config.port can not be 0!");
            return;
        }

        WTFSocketSessionFactory.config = config;

        // 每次复位，主会话将会保留
        // 心跳会话将会重新开启
        EMPTY = getSession("empty");
        SERVER = getSession("server");
        if (HEARTBEAT != null) {
            HEARTBEAT.close();
        }
        HEARTBEAT = getSession("heartbeat");

        WTFSocketSessionFactory.socketClient = new WTFSocketBootstrap(config);
        socketClient.start();
    }

    /**
     * 反初始化
     */
    public static void deInit() {
        socketClient.close();
        isAvailable = false;
        for (WTFSocketEventListener listener : eventListeners) {
            listener.onDisconnect();
        }
    }

    /**
     * 复位
     */
    public static void reInit() {
        if (isAvailable) {
            deInit();
        }
        init(config);
    }

    /**
     * 获取的会话
     * 如果会话不存在会自动创建会话
     *
     * @param to 目标地址
     * @return WTFSocketSession
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
        if (sessions.containsKey(session.getTo())) {
            sessions.remove(session.getTo());
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
            for (WTFSocketEventListener listener : eventListeners) {
                listener.onNewSession(session, msg);
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

    // 获取心跳监听者
    static List<WTFSocketEventListener> getEventListeners() {
        return eventListeners;
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
    static boolean dispatchException(WTFSocketException e, WTFSocketMsgWrapper msgWrapper) {

        if (msgWrapper == null) {
            msgWrapper = new WTFSocketMsgWrapper();
        }

        WTFSocketSession session = msgWrapper.getBelong();

        if (!session.dispatchException(msgWrapper, e)) {
            // 会话对象无法处理消息
            // 使用默认响应函数
            if (!defaultResponse.onException(msgWrapper.getBelong(), msgWrapper.getMsg(), e)) {
                return printHandler.onException(session, msgWrapper.getMsg(), e);
            }

        }

        return true;
    }

    // 异常派发
    static boolean dispatchException(WTFSocketException e) {

        return dispatchException(e, null);
    }

}
