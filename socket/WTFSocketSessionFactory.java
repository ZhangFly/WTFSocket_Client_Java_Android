package wtf.socket;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * socket会话工厂
 * 负责创建会话和分发消息
 * 会话工厂初始化后会自动创建一个连接到服务器的会话 SERVER
 */
public class WTFSocketSessionFactory {

    private static final WTFSocketHandler DEFAULT_RESPONSE = new WTFSocketHandler() {
    };

    private static final WTFSocketEncoder DEFAULT_ENCODER = new WTFSocketEncoder() {
        @Override
        public byte[] encode(String data) {
            return data.getBytes();
        }
    };

    private static final WTFSocketDecoder DEFAULT_DECODER = new WTFSocketDecoder() {
        @Override
        public String decode(byte[] data, int len) {
            return new String(data, 0, len);
        }
    };

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

    // 自增的消息ID
    private static AtomicInteger msgId = new AtomicInteger(1);

    // 事件监听者
    private static List<WTFSocketEventListener> eventListeners = new ArrayList<>();

    // 默认响应方法
    private static WTFSocketHandler defaultResponse = DEFAULT_RESPONSE;

    // 执行handler方法
    private static ExecutorService executor = Executors.newCachedThreadPool();

    // 编码器
    private static WTFSocketEncoder encoder = DEFAULT_ENCODER;

    // 解码器
    private static WTFSocketDecoder decoder = DEFAULT_DECODER;


    // 打印方法
    // 当消息/异常没有被任何方法响应时
    // 会调用该方法打印
    private static WTFSocketHandler printHandler = new WTFSocketHandler() {
        @Override
        public boolean onReceive(WTFSocketSession session, WTFSocketMsg msg) {
            WTFSocketLogUtils.info(String.format(
                    "printHandler: receive msg from <%s> to <%s>:\nmsg => %s",
                    session.getFrom(),
                    session.getTo(),
                    msg
            ));
            return true;
        }

        @Override
        public boolean onException(WTFSocketSession session, WTFSocketMsg msg, WTFSocketException e) {
            WTFSocketLogUtils.err(String.format(
                    "printHandler: occur exception from <%s> to <%s>:\nmsg => %s\n%s",
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

        WTFSocketBootstrap.start();
    }

    /**
     * 反初始化
     * 不会关闭监听线程
     */
    public static void deInit() {

        if (isAvailable) {
            WTFSocketBootstrap.close();
            isAvailable = false;
        }
        for (WTFSocketSession session : sessions.values()) {
            session.close();
        }
        notifyEventListeners(WTFSocketEventType.DISCONNECT);
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
     * 终止框架
     * 并关闭监听线程
     */
    public static void shutdown() {
        WTFSocketBootstrap.shutdown();
    }

    /**
     * 获取的会话
     * 如果会话不存在会自动创建会话
     *
     * @param to 目标地址
     * @return wtf.WTFSocketSession
     */
    public static WTFSocketSession getSession(String to) {

        return getSession(to, new WTFSocketMsg());
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
        defaultResponse = DEFAULT_RESPONSE;
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

    /**
     * 获取编码器
     *
     * @return 编码器
     */
    public static WTFSocketEncoder getEncoder() {
        return encoder;
    }

    /**
     * 设置编码器
     *
     * @param encoder 编码器
     */
    public static void setEncoder(WTFSocketEncoder encoder) {
        if (encoder != null) {
            WTFSocketSessionFactory.encoder = encoder;
        } else {
            WTFSocketSessionFactory.encoder = DEFAULT_ENCODER;
        }
    }

    /**
     * 获取解码器
     *
     * @return 解码器
     */
    public static WTFSocketDecoder getDecoder() {
        return decoder;
    }

    /**
     * 设置解码器
     *
     * @param decoder 解码器
     */
    public static void setDecoder(WTFSocketDecoder decoder) {
        if (encoder != null) {
            WTFSocketSessionFactory.decoder = decoder;
        } else {
            WTFSocketSessionFactory.decoder = DEFAULT_DECODER;
        }
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
    static void dispatchMsg(final WTFSocketMsgWrapper msgWrapper) {

        executor.submit(new Runnable() {

            @Override
            public void run() {
                // 处理心跳包
                if (msgWrapper.getMsgType() == 0) {
                    HEARTBEAT.dispatchMsg(msgWrapper);
                    return;
                }

                // 处理普通消息
                WTFSocketSession session = getSession(msgWrapper.getFrom(), msgWrapper.getMsg());

                if (!session.dispatchMsg(msgWrapper)) {
                    if (!defaultResponse.onReceive(session, msgWrapper.getMsg())) {
                        printHandler.onReceive(session, msgWrapper.getMsg());
                    }
                }
            }
        });
    }

    // 异常派发
    static void dispatchException(WTFSocketException e) {
        dispatchException(e, null);
    }

    // 异常派发
    static void dispatchException(final WTFSocketException e, WTFSocketMsgWrapper wrapper) {

        final WTFSocketMsgWrapper msgWrapper = wrapper == null ? WTFSocketMsgWrapper.empty() : wrapper;

        executor.submit(new Runnable() {
            @Override
            public void run() {

                WTFSocketSession session = msgWrapper.getBelong();

                if (!session.dispatchException(e, msgWrapper)) {
                    // 会话对象无法处理消息
                    // 使用默认响应函数
                    if (!defaultResponse.onException(msgWrapper.getBelong(), msgWrapper.getMsg(), e)) {
                        printHandler.onException(session, msgWrapper.getMsg(), e);
                    }

                }
            }
        });
    }

    // 从注册表中将 session 移除
    static void unRegisterSession(WTFSocketSession session) {
        if (sessions.contains(session)) {
            sessions.remove(session.getTo());
        }
    }

    // 通知监听者
    static void notifyEventListeners(final WTFSocketEventType type, final Object... params) {

        for (final WTFSocketEventListener listener : eventListeners) {

            executor.submit(new Runnable() {
                @Override
                public void run() {
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
            });
        }
    }

    // 获取全局配置
    static WTFSocketConfig getConfig() {
        return config;
    }
}
