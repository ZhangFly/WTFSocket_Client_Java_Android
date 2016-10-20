# WTFSocket

**Brief**

适用于使用一个socket客户端，和多个对象的通话的场景。

**Protocol**

```js
{
    "from" : String,       // @necessary
    "to" : String,         // @necessary
    "msgId": int,          // @necessary
    "msgType": int,        // @necessary
    "flag" : int,          // @option
    "errCode" : int,       // @option
    "cmd" : int,           // @option
    "params" : Array<JSON> // @option
}\r\n                      // @necessary

```

**Need**

`JDK-1.7`／`Android-6.0` or later

**Dependences**

[`fastjson-1.2.9.jar`](http://mvnrepository.com/artifact/com.alibaba/fastjson/1.2.9)

**Document**

[`gitbook`](https://zoutstanding.gitbooks.io/wtfsocket/content/)

