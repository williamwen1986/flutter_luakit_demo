## 使用flutter\_luakit\_plugin作为基础库开发flutter应用

文章开头我们先开门见山给出使用flutter\_luakit\_plugin作为基础库开发和普通flutter的区别。由于flutter定位是便携UI包，flutter提供的基础库功能是不足以满足复杂数据的app应用的，一般flutter开发模式如下图所示，当flutter满足不了我们的需求的时候，使用methodchannel和eventchannel调用native接口。
![image](https://raw.githubusercontent.com/williamwen1986/Luakit/master/image/flutter2native.jpeg)

而使用flutter\_luakit\_plugin作为基础库的开发模式如下图所示，用lua来写逻辑层代码，用flutter写UI代码。luakit 提供了丰富的功能支持，可以支持大部分app的逻辑层开发，包括数据库orm，线程管理，http请求，异步socket，定时器，通知，json等等。**用户只需要写dart代码和lua代码，不需要写oc、swift或java、kotlin代码，从而大幅提升代码的一致性（所有运行代码都是跨平台的）**。
![image](https://raw.githubusercontent.com/williamwen1986/Luakit/master/image/flutter2lua.jpg)

## flutter\_luakit\_plugin由来
    
Flutter诞生的时候我很兴奋，因为我对跨平台开发UI的看法一直是不看好的，最主要的原因是无法获得体验一致性，但是Flutter前无古人的解决了这个问题，真正做到一端开发的UI，无论多复杂，在另一端是可以得到一致的体验的，做不到这点的跨平台UI方案实际上并没有达到跨平台节省工作量的效果，Flutter做到了。

Flutter1.0.0 发布了，我认为移动端跨平台开发所需要所有元素都已经齐备了，我们尝试使用Flutter做一些功能，一个版本之后我们总结了一些问题。

* Flutter是一套UI解决方案，但一个功能除了UI，还需要很多支持，网络请求，长连接，短连接，数据库，线程控制等等，这些方面Flutter生态中提供得比较差，没有ios 或者android那么多成熟的解决方案。Flutter 为了克服这问题，提供了一个解决方案，利用methodchannel和eventchannel调用ios和android的接口，利用原生成熟的方案做底层逻辑支撑。我们一开始也是这样解决，但后续的麻烦也来了，由于methodchannel和eventchannel实现的方法是不跨平台的，Flutter从ios和android得到的数据的格式，事件调用的时机等，两个平台的实现是不一样的，基本不可能完全统一，可以这样说，一个功能在一个端能跑通，**在另一个端第一次跑一定跑不通**，然后就要花大量的时间进行调试，适配，这样做之后跨平台的优势荡然无存,大家就会不断扯皮。相信我，下面的对话会成为你们的日常。



    ios开发：“你们android写的界面ios跑不起来”
    
    Android 开发：“我们android能跑啊，iOS接口写得不对吧”
    
    ios开发：“哪里不对，android写的界面，android帮忙调吧”
    
    Android 开发：“我又不是ios开发，我怎么调”
    
    
    
* 当一个已有的app要接入flutter，必然会产生一种情况，就是flutter体系里面的数据和逻辑，跟外部原生app的逻辑是不通的，简单说明一下，就是flutter写的业务逻辑通常是用dart语言写的，我们在原生用object-c、swift或者java、kotlin写的代码是不可以脱离flutter的界面调用dart写的逻辑的，这种互通性的缺失，会导致很多数据联动做不到，譬如原生界面要现实一个flutter页面存下来的数据，或者原生界面要为flutter页面做一些预加载，这些都很不方便，主要是下图中，当flutter界面没调用时，从原生调用flutter接口是不允许的。 
<div align=center>
![image](https://raw.githubusercontent.com/williamwen1986/Luakit/master/image/flutter1.jpg)
</div>


之前我曾经开源一个纯逻辑层的跨平台解决方案[luakit](https://github.com/williamwen1986/Luakit)([附上luakit的起源](https://www.jianshu.com/p/5b15d20ef797))，里面提供一个业务开发所需要的基本能力，包括网络请求，长连接，短连接，orm数据库，线程，通知机制等等，而且这些能力都是稳定的、跨平台而且经过实际业务验证过的方案。

做完一个版本纯flutter之后，我意识到可以用一种新的开发模式来进行flutter开发，这样可以避免我上面提到的两个问题，我们团队马上付诸实施，做了另一个版本的flutter+luakit的尝试，即用flutter做界面，用lua来写逻辑，结构图如下。
<div align=center>
![image](https://raw.githubusercontent.com/williamwen1986/Luakit/master/image/flutter2.jpg)
</div>
**新的方案开发效率得到极大的提升，不客气的说真正实现了跨平台**，一个业务，从页面到逻辑，所有的代码一气呵成全部由脚本完成（dart+lua），完全不用object-c、swift或者java、kotlin来写逻辑，这样一个业务基本就可以无缝地从一端直接搬到另一端使用，所以我写了这篇文章来介绍我们团队的这个尝试，也把我们的成果[flutter\_luakit\_plugin](https://pub.dartlang.org/packages/flutter_luakit_plugin)开源了出来，让这种开发模式帮助到更多flutter开发团队。

## 细说开发模式

下一步我们一起看看如何用flutter配合lua实现全部代码都是跨平台的。我们提供了一个 [demo project](https://github.com/williamwen1986/flutter_luakit_demo)，供大家参考。

* dart写界面

	在demo中所有的ui都写在了[main.dart](https://github.com/williamwen1986/flutter_luakit_demo/blob/master/lib/main.dart),当然在真实业务中肯定复杂很多，但是并不影响我们的开发模式。
	
* dart调用lua逻辑接口

```
FlutterLuakitPlugin.callLuaFun("WeatherManager", "getWeather").then((dynamic d) {
  print("getWeather" + d.toString());
  setState(() {
    weathers = d;
  });
});
```
上面这段代码的意思是调用WeatherManager的lua模块，里面提供的getWeather方法，然后把得到的数据以future的形式返回给dart，上面的代码相当于调用下面一段lua代码


```lua
require('WeatherManager').getWeather( function (d) 
   
end)
```

然后剩下的事情就到lua，在lua里面可以使用luakit提供的所有强大功能，一个app所需要的绝大部分的功能应该都提供了，而且我们还会不断扩展。

大家可能会担心dart和lua的数据格式转换问题，这个不用担心，所有细节在flutter\_luakit\_plugin都已经做好封装，使用者尽管像使用dart接口那样去使用lua接口即可。

* 在lua中实现所有的非UI逻辑

	这个[demo(WeatherManager.lua)](https://github.com/williamwen1986/flutter_luakit_demo/blob/master/android/app/src/main/assets/lua/WeatherManager.lua)已经演示了如何使用luakit的相关功能，包括，网络，orm数据库，多线程，数据解析，等等
	
* 如果实在有flutter\_luakit\_plugin没有支持的功能，可以走回flutter提供的methodchannel和eventchannel的方式实现

## 如何接入flutter\_luakit\_plugin

经过了几个月磨合实践，我们团队已经把接入flutter\_luakit\_plugin的成本降到最低，可以说是非常方便接入了。我们已经把flutter\_luakit\_plugin发布到flutter官方的插件仓库。首先，要像其他flutter插件一样，在pubspec.yaml里面加上依赖，可参考[demo配置](https://github.com/williamwen1986/flutter_luakit_demo/blob/master/pubspec.yaml)

```
flutter_luakit_plugin: ^1.0.0
```

然后在ios项目的podfile加上ios的依赖，可参考[demo配置](https://github.com/williamwen1986/flutter_luakit_demo/blob/master/ios/Podfile)

```
source 'https://github.com/williamwen1986/LuakitPod.git'
source 'https://github.com/williamwen1986/curl.git'
pod 'curl', '~> 1.0.0'
pod 'LuakitPod', '~> 1.0.13'
```

然后在android项目app的build.gradle文件加上android的依赖，可参考[demo配置](https://github.com/williamwen1986/flutter_luakit_demo/blob/master/android/app/build.gradle)


```
repositories {
    maven { url "https://jitpack.io" }
}

dependencies {
    implementation 'com.github.williamwen1986:LuakitJitpack:1.0.6'
}

```
最后，在需要使用的地方加上import就可以使用lua脚本了

```
import 'package:flutter_luakit_plugin/flutter_luakit_plugin.dart';
```

lua脚本我们默认的执行根路径在android是 assets/lua，ios默认的执行根路径是Bundle路径。

## flutter\_luakit\_plugin开发环境IDE--AndroidStudio

flutter 官方推荐的IDE是androidstudio和visual studio code。我们在开发中觉得androidstudio更好用，所有我们同步也开发了luakit的androidstudio
插件，名字就叫luakit。luakit插件提供了以下的一些功能。

* 远程lua调试
* 查找函数使用
* 跳到函数定义
* 跳到文件
* 参数名字提示
* 代码自动补全
* 代码格式化
* 代码语法检查
* 标准lua api自动补全
* luakit api自动补全

大部分功能，跟其他IDE没太多差别，这里我就不细讲了，我重点讲一下远程lua调试功能，因为这个跟平时调试ios和android设备有点不一样，下面我们详细介绍androidstudio luakit插件的使用。

**androidstudio安装luakit插件**

AndroidStudio->Preference..->Plugins->Browse reprositories...

![image](https://raw.githubusercontent.com/williamwen1986/Luakit/master/image/1.jpeg)

搜索Luakit并安装Luakit插件然后重启androidstudio

![image](https://raw.githubusercontent.com/williamwen1986/Luakit/master/image/2.jpeg)

**配置lua项目**

打开 Project Struture 窗口

![image](https://raw.githubusercontent.com/williamwen1986/Luakit/master/image/3.jpeg)

选择 Modules、 Mark as Sources

![image](https://raw.githubusercontent.com/williamwen1986/Luakit/master/image/4.jpeg)

**添加调试器**

选择 Edit Configurations ...

![image](https://raw.githubusercontent.com/williamwen1986/Luakit/master/image/5.jpeg)

Select plus

![image](https://raw.githubusercontent.com/williamwen1986/Luakit/master/image/6.jpeg)

添加Lua Remote(Mobdebug)

![image](https://raw.githubusercontent.com/williamwen1986/Luakit/master/image/7.png)

**远程lua调试**

在开始调试lua之前，我们要在需要调试的lua文件加上下面一句lua代码。然后设上断点，即可调试。lua代码里面有两个参数，第一个是你调试用的电脑的ip地址，第二个是调试端口，默认是8172。

```lua
require("mobdebug").start("172.25.129.165", 8172)
```

![image](https://raw.githubusercontent.com/williamwen1986/Luakit/master/image/9.jpeg)

luakit的调试是通过socket来传递调试信息的，所有调试机器务必我电脑保持在同一网段，有时候可能做不到，这里我们给出一下办法解决，我们日常调试也是这样解决的。首先让你的手机开热点，然后你的电脑连上手机的热点，现在就可以保证你的手机和电脑是同一网段了，然后查看电脑的ip地址，填到lua代码上，就可以实现调试了。

![image](https://raw.githubusercontent.com/williamwen1986/Luakit/master/image/10.jpeg)

![image](https://raw.githubusercontent.com/williamwen1986/Luakit/master/image/11.jpeg)


## flutter\_luakit\_plugin提供的api介绍

（1） 数据库orm操作

这是flutter\_luakit\_plugin里面提供的一个强大的功能，也是flutter现在最缺的，简单高效的数据库操作，flutter\_luakit\_plugin提供的数据库orm功能有以下特征

* 面向对象
* 自动创建和更新表结构
* 自带内部对象缓存
* 定时自动transaction
* 线程安全，完全不用考虑线程问题

具体可参考[demo lua](https://github.com/williamwen1986/Luakit/blob/master/LuaKitProject/src/Projects/LuaSrc/db_test.lua)，下面只做简单介绍。

**定义数据模型**

```lua
-- Add the define table to dbData.lua
-- Luakit provide 7 colum types
-- IntegerField to sqlite integer 
-- RealField to sqlite real 
-- BlobField to sqlite blob 
-- CharField to sqlite varchar 
-- TextField to sqlite text 
-- BooleandField to sqlite bool
-- DateTimeField to sqlite integer
user = {
	__dbname__ = "test.db",
	__tablename__ = "user",
	username = {"CharField",{max_length = 100, unique = true, primary_key = true}},
	password = {"CharField",{max_length = 50, unique = true}},
	age = {"IntegerField",{null = true}},
	job = {"CharField",{max_length = 50, null = true}},
	des = {"TextField",{null = true}},
	time_create = {"DateTimeField",{null = true}}
	},
-- when you use, you can do just like below
local Table = require('orm.class.table')
local userTable = Table("user")
```

**插入数据**

```lua
local userTable = Table("user")
local user = userTable({
	username = "user1",
	password = "abc",
	time_create = os.time()
})
user:save()
```

**更新数据**

```lua
local userTable = Table("user")
local user = userTable.get:primaryKey({"user1"}):first()
user.password = "efg"
user.time_create = os.time()
user:save()
```
**删除数据**

```lua
local userTable = Table("user")
local user = userTable.get:primaryKey({"user1"}):first()
user:delete()
```

**批量更新数据**

```lua
local userTable = Table("user")
userTable.get:where({age__gt = 40}):update({age = 45})
```

**批量删除数据**

```lua
local userTable = Table("user")
userTable.get:where({age__gt = 40}):delete()
```
**select数据**

```lua
local userTable = Table("user")
local users = userTable.get:all()
print("select all -----------")
local user = userTable.get:first()
print("select first -----------")
users = userTable.get:limit(3):offset(2):all()
print("select limit offset -----------")
users = userTable.get:order_by({desc('age'), asc('username')}):all()
print("select order_by -----------")
users = userTable.get:where({ age__lt = 30,
	age__lte = 30,
	age__gt = 10,
	age__gte = 10,
	username__in = {"first", "second", "creator"},
	password__notin = {"testpasswd", "new", "hello"},
	username__null = false
	}):all()
print("select where -----------")
users = userTable.get:where({"scrt_tw",30},"password = ? AND age < ?"):all()
print("select where customs -----------")
users = userTable.get:primaryKey({"first","randomusername"}):all()
print("select primaryKey -----------")
```

**联表操作**

```lua
local userTable = Table("user")
local newsTable = Table("news")
local user_group = newsTable.get:join(userTable):all()
print("join foreign_key")
user_group = newsTable.get:join(userTable,"news.create_user_id = user.username AND user.age < ?", {20}):all()
print("join where ")
user_group = newsTable.get:join(userTable,nil,nil,nil,{create_user_id = "username", title = "username"}):all()
print("join matchColumns ")
```


（2） 通知机制

通知机制提供了一个低耦合的事件互通方法，即在原生或者lua或者dart注册消息，在任何地方抛出的消息都可以接收到。

**Flutter 添加监听消息**

```
void notify(dynamic d) {

}

FlutterLuakitPlugin.addLuaObserver(3, notify);

```

**Flutter 取消监听**

```
FlutterLuakitPlugin.removeLuaObserver(3, notify);
```

**Flutter抛消息**

```
FlutterLuakitPlugin.postNotification(3, data);
```

**lua 添加监听消息**[demo code](https://github.com/williamwen1986/Luakit/blob/master/LuaKitProject/src/Projects/LuaSrc/notification_test.lua)


```lua
local listener

lua_notification.createListener(function (l)
	listener = l
	listener:AddObserver(3,
	    function (data)
	        print("lua Observer")
	        if data then
	            for k,v in pairs(data) do
	                print("lua Observer"..k..v)
	            end
	        end
	    end
	)
end);
```

**lua抛消息**[demo code](https://github.com/williamwen1986/Luakit/blob/master/LuaKitProject/src/Projects/LuaSrc/notification_test.lua)


```lua
lua_notification.postNotification(3,
{
    lua1 = "lua123",
    lua2 = "lua234"
})
```

**ios 添加监听消息**[demo code](https://github.com/williamwen1986/Luakit/blob/master/LuaKitProject/IOS%20Demo/NotificationTest/NotificationTest/ViewController.mm)

```	objective-c
_notification_observer.reset(new NotificationProxyObserver(self));
_notification_observer->AddObserver(3);
- (void)onNotification:(int)type data:(id)data
{
    NSLog(@"object-c onNotification type = %d data = %@", type , data);
}
```

**ios抛消息**[demo code](https://github.com/williamwen1986/Luakit/blob/master/LuaKitProject/IOS%20Demo/NotificationTest/NotificationTest/ViewController.mm)

```	objective-c
post_notification(3, @{@"row":@(2)});
```

**android 添加监听消息**[demo code](https://github.com/williamwen1986/Luakit/blob/master/LuaKitProject/Android%20Demo/NotificationTest/app/src/main/java/luakit/com/notificationtest/MainActivity.java)

```java
LuaNotificationListener  listener = new LuaNotificationListener();
INotificationObserver  observer = new INotificationObserver() {
    @Override
    public void onObserve(int type, Object info) {
        HashMap<String, Integer> map = (HashMap<String, Integer>)info;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            Log.i("business", "android onObserve");
            Log.i("business", entry.getKey());
            Log.i("business",""+entry.getValue());
        }
    }
};
listener.addObserver(3, observer);
```
**android抛消息**[demo code](https://github.com/williamwen1986/Luakit/blob/master/LuaKitProject/Android%20Demo/NotificationTest/app/src/main/java/luakit/com/notificationtest/MainActivity.java)

```java

HashMap<String, Integer> map = new HashMap<String, Integer>();
map.put("row", new Integer(2));
NotificationHelper.postNotification(3, map);
```

（3） http request

flutter本身提供了http请求库dio，不过当项目的逻辑接口想在flutter，原生native都可用的情况下，flutter写的逻辑代码就不太合适了，原因上文已经提到，原生native是不可以随意调用flutter代码的，所以遇到这种情况，只有luakit合适，lua写的逻辑接口可以在所有地方调用，flutter 、ios、android都可以方便的使用lua代码，下面给出luakit提供的http接口，[demo code](https://github.com/williamwen1986/Luakit/blob/master/LuaKitProject/src/Projects/LuaSrc/WeatherManager.lua)。

```lua
-- url , the request url
-- isPost, boolean value represent post or get
-- uploadContent, string value represent the post data
-- uploadPath,  string value represent the file path to post
-- downloadPath, string value to tell where to save the response
-- headers, tables to tell the http header
-- socketWatcherTimeout, int value represent the socketTimeout
-- onResponse, function value represent the response callback
-- onProgress, function value represent the onProgress callback
lua_http.request({ url  = "http://tj.nineton.cn/Heart/index/all?city=CHSH000000",
	onResponse = function (response)
	end})
```

（4） Async socket

异步socket长连接功能也是很多app开发所依赖的，flutter只支持websocket协议，如果app想使用基础的socket协议，那就要使用flutter\_luakit\_plugin提供的socket功能了，使用也非常简单，[demo code](https://github.com/williamwen1986/Luakit/blob/master/LuaKitProject/src/Projects/LuaSrc/async_socket_test.lua)，在callback里面拿到数据后可以使用上文提到的通知机制把数据传回到flutter层。

```lua
local socket = lua_asyncSocket.create("127.0.0.1",4001)

socket.connectCallback = function (rv)
    if rv >= 0 then
        print("Connected")
        socket:read()
    end
end
    
socket.readCallback = function (str)
    print(str)
    timer = lua_timer.createTimer(0)
    timer:start(2000,function ()
        socket:write(str)
    end)
    socket:read()
end

socket.writeCallback = function (rv)
    print("write" .. rv)
end

socket:connect()
```

（5） json 解析

json是最常用数据类型，使用可参考[demo](https://github.com/williamwen1986/Luakit/blob/master/LuaKitProject/src/Projects/LuaSrc/WeatherManager.lua)

```lua

local t = cjson.decode(responseStr)

responseStr = cjson.encode(t)

```

(6)  定时器timer

定时器也是项目开发中经常用到的一个功能，定时器我们在orm框架的lua源码里面有用到，[demo](https://github.com/williamwen1986/Luakit/blob/master/LuaKitProject/src/Projects/LuaSrc/orm/model.lua)

```lua

local _timer

_timer = lua_timer.createTimer(1)//0代表单次，1代表重复

_timer:start(2000,function ()
    
end)

_timer:stop()

```

(7) 还有所有普通适合lua用的库都可以在flutter\_luakit\_plugin使用


## flutter技术积累相关链接

<font color=#0099ff size=5 face="黑体">[flutter通用基础库flutter\_luakit_plugin](https://github.com/williamwen1986/flutter_luakit_demo)</font>

<font color=#0099ff size=5 face="黑体">[flutter\_luakit\_plugin使用例子](https://github.com/williamwen1986/flutter_luakit_demo)</font>

<font color=#0099ff size=5 face="黑体">[《手把手教你编译Flutter engine》](https://juejin.im/post/5c24acd5f265da6164141236 )</font>

<font color=#0099ff size=5 face="黑体">[《手把手教你解决 Flutter engine 内存漏》](https://juejin.im/post/5c24ad306fb9a049d2361cff)</font>

<font color=#0099ff size=5 face="黑体">[修复内存泄漏后的flutter engine（可直接使用）](https://github.com/Natoto/fixFlutterEngine)</font>

<font color=#0099ff size=5 face="黑体">[修复内存泄漏后的flutter engine使用例子](https://github.com/Natoto/flutterOnExistApp/tree/multiflutter)</font>

<font color=#0099ff size=5 face="黑体">持续更新中...</font>

