# 基于 OkHttp 轻量级封装，一个 API 接口生成一个实体类。

## 快速使用

### 接口定义：

```
//定义接口：post=OkPost get=OkGet content-type=ContentType.FORM || JSON
@OkPost(url = "http://localhost:8080/",type = ContentType.FORM)
public interface NewsNetworkApi {
    @OkPost(uri = "news/detail")
    @OkConfig(log = "获取新闻列表")
    Result<List<News>> newsList(String newsId);
}
```

### 使用接口（同步）

```
Result<List<News>> result= NewsList.api().newsId("123").execute();
```

### 使用接口（异步）

```
NewsList.api().newsId("123").enqueue(new OkCallback<Result<List<News>>>() {
    @Override
    public void onSucceed(Result<List<News>> entity) {

    }

    @Override
    public void onError(int code, String errorBody, Exception e) {

    }
});
```
## Android 集成
(默认 UI 线程回调，需要异步线程回调请使用 inThread 方法)<br/><br/>
引包，在 app/build.gradle 文件下：
```
dependencies {
	//引用OKHttp关联框架
    implementation 'com.squareup.okhttp3:okhttp:3.2.0'
    //引用Gson关联框架
    implementation 'com.google.code.gson:gson:2.8.6'

	//引用OkReduce核心包
    implementation files('libs/okreduce-android-0.1.aar')
    //开启OkReduce注解器
    annotationProcessor files('libs/okreduce-compiler-0.1.jar')
}
```

## Java 集成（IntelliJ IDEA 、Eclipse 、Maven）

(Eclipse 需要安装 m2e-apt 插件，并开启 annotation processing 支持) <br/><br/>
引包，在 pom.xml 文件下：

```
//引用OkReduce核心包
<dependency>
	<groupId>okreduce</groupId>
	<artifactId>okreduce-java</artifactId>
	<scope>system</scope>
	<version>0.1</version>
	<systemPath>${project.basedir}/src/main/resources/libs/okreduce-java-0.1.jar</systemPath>
</dependency>
//引用OkReduce注解器
<dependency>
	<groupId>okreduce</groupId>
	<artifactId>okreduce-compiler</artifactId>
	<scope>system</scope>
	<version>0.1</version>
	<systemPath>${project.basedir}/src/main/resources/libs/okreduce-compiler-0.1.jar</systemPath>
</dependency>

//引用OKHttp关联框架
<dependency>
	<groupId>com.squareup.okhttp3</groupId>
	<artifactId>okhttp</artifactId>
	<version>4.9.1</version>
</dependency>
//引用Gson关联框架
<dependency>
	<groupId>com.google.code.gson</groupId>
	<artifactId>gson</artifactId>
	<version>2.8.6</version>
</dependency>
```
支持properties文件全局配置方式，配置示例请参考源码中的okconfig.properties文件。<br/>
加载配置文件的方法：OkGlobalConfig.loadConfig("文件路径")

## 高级使用

### 更多接口定义

```
@OkPost(url = "http://localhost:8080/",type = ContentType.FORM,
        urls = {" http://localhost2:8080/"," http://localhost3:8080/"})
@OkConfig(connectout = 3000,readout = 3000,writeout = 3000,retry = 1,debug = true)
public interface NewsNetworkApi {
    @OkPost(uri = "auth/login",type = ContentType.JSON)
    @OkConfig(log = "用户登录")
    String login(String name, String password);

    @OkPost(uri = "auth/register")
    @OkConfig(log = "用户注册")
    void register(@OkHead String userAgent,@OkEntity User user);

    @OkPost(uri = "user/info")
    @OkConfig(log = "用户信息")
    User userDetail(@OkParam("USER_ID") String userId);

    @OkPost(uri = "user/friendList")
    @OkConfig(log = "获取好友列表")
    List<User> friendList();

    @OkPost(uri = "news/detail")
    @OkConfig(log = "获取新闻列表")
    Result<List<News>> newsList(String newsId);

    @OkPost(uri = "news/info")
    @OkConfig(log = "获取新闻信息")
    Map<String, Object> newsInfo(String newsId);

    @OkGet(url = "https://down.qq.com/qqweb/PCQQ/PCQQ_EXE/PCQQ2020.exe")
    @OkConfig(log = "下载文件")
    File download();

    @OkPost(uri = "api/app/common/uploadFile")
    @OkConfig(log = "上传文件")
    String uploadFile(String id,File file1,File file2);
}
```

### 全局配置(单一对象，可多次调用)

可配置 Interceptor、SSLSocketFactory、IParser、主 url 与备用 url、全局参数与头信息、超时与重试等。<br/>
(接口调用配置 > 接口配置 > 全局配置)

```
OkGlobalConfig.builder().url("http://localhost:8080/","http://localhost2:8080/")
    .debug(true)
    .interceptor(new Interceptor() {
		@Override
		public Response intercept(Chain chain) throws IOException {
			return null;
			}
		})
	.ssl(sslSocketFactory, hostnameVerifier)
    .savePath(new File("E:\\OkDownload"));
```

### 注解类型说明

| 注解名         | 方法       | 值         | 作用域   | 说明                               |
| -------------- | ---------- | --------   | -------- | ---------------------------------- |
| OkPost/OkGet   | url        | String     | 类与方法 | 请求 url 地址                      |
| ．．．．       | uri        | String     | 类与方法 | 请求 uri 地址                      |
| ．．．．       | urls       | String[]   | 类与方法 | 请求备用 url 地址                  |
| ．．．．       | type       | ContentType| 类与方法 | 请求 Content-Type，可选 FORM、JSON |
| OkParam/OkHead | value      | String     | 参数字段 | 参数 key                           |
| ．．．．       | initial    | String     | 参数字段 | 参数默认值                         |
| OkEntity       | ．．．．   | ．．．．   | 参数字段 | 声明为实体参数                     |
| OkConfig       | connectout | int        | 类与方法 | 连接超时                           |
| ．．．．       | readout    | int        | 类与方法 | 读超时                             |
| ．．．．       | writeout   | int        | 类与方法 | 写超时                             |
| ．．．．       | retry      | int        | 类与方法 | 重试次数，0 为不重试               |
| ．．．．       | debug      | boolean    | 类与方法 | 调试模式                           |
| ．．．．       | log        | String     | 类与方法 | 调试日志输出                       |
