# WTFSocket

**Brief**

适用于使用一个socket客户端，和多个对象的通话的场景。

**Need**

`JDK-1.7`／`Android-6.0` or later

**Guide**

框架的集成主要包括以下几步:

1. 将文件添加入工程。
2. 将依赖的包添加如工程。
3. 初始化框架。
4. 创建到指定对象的会话。
5. 发送消息。

```java

// step 1
// 导入文件
import wtf.socket.*;

// step 2
// 初始化框架
WTFSocketSessionFactory.init(
    new WTFSocketConfig()
        .setIp("xxx.xxx.xxx.xxx")
        .setPort(1234)
        .setLocalName("whatever")
);

// step 3
// 创建到指定对象的会话
WTFSocketSession session = WTFSocketSessionFactory.getSession("targetName");

// step 4
// 创建到指定对象的会话
session.sendMsg(new WTFSocketMsg()); 

// finish
// so easy
// 更多功能请参看详细文档

```

**Dependences**

[`fastjson-1.2.9`](http://mvnrepository.com/artifact/com.alibaba/fastjson/1.2.9)

[`commons-lang-2.6`](http://mvnrepository.com/artifact/commons-lang/commons-lang/2.6)

**Document**

[`gitbook`](https://zoutstanding.gitbooks.io/wtfsocket-client-java-android/content/)
