1.功能描述:

本模块为http服务调用脚手架,提供依靠api即可完成http
调用的功能(类似RPC)

2.效果展示: 

SharingStrategyFuncService为A模块的服务接口,
AuthorizationFuncServiceImpl为B模块的服务实现因为Spring Cloud为了降低耦合采用http通信，若是B
要调用A的SharingStrategyFuncService服务中的方法则要发送http请求,
代码大量重复,且http连接不易管理,故此封装了一套框架,在此基础之上
调用只需要简单注入如: 
@Autowired
SharingStrategyFuncService sharingStrategyFuncService;
在代码中即可直接调用,如
sharingStrategyFuncService.sharingStrategy(request);
和本地服务调用毫无区别

3.使用说明:

将base-expand模块下载(使用源码或者jar包方式皆可,
推荐源码方式,可以自行修改定制),在api模块即服务接口所在模块
引入base-expand依赖(base-expand模块已引入spring-boot-starter-web
和spring-cloud-starter-netflix-eureka-client故微服务模块这两个jar无需再次引入),
引入之后在生产者模块需要被远程调用的服务接口上加上RemoteInfc,在服务实现上加上RemoteService,
然后在消费者模块的classpath下配置remote-service.json,内容格式如下:

[
  {
  
    "serviceId": "100000",
    "remoteInfcName": "com.asiainfo.strategy.service.SharingStrategyFuncService",
    "serviceCenter": "sharing-strategy-impl",
    "serviceVersion": "1.0.0",
    "methodName": "sharingStrategy",
    "requestType": "com.asiainfo.frame.base.RequestBase"
  },
  
  {
  
    "remoteInfcName": "com.asiainfo.strategy.service.SharingStrategyFuncService",
    "serviceCenter": "sharing-strategy-impl"
  }
]

这里有两种模式,第二种只有remoteInfcName和serviceCenter,用于仅被内部模块调用的服务,
第一种更多参数的模式用于对外服务(一般是提供给前端)的统一调用,调用形式为
"ip:port/common/invoke?serviceId=配置serviceId",需要注意的是第一种用于对外服务的
模式由于http的body是统一的,所以此类接口仅支持单请求参数(相对的如果是内部调用可以支持任意
个数的请求参数,目前支持基本类型及其包装类、Date、LocalDateTime、Map和Collection),
此外还需注意包的扫描,需在Spring boot启动类的@SpringBootApplication注解中加入SpringBootApplication
scanBasePackages = {"com.asiainfo.frame"},以使配置类及处理类被扫描并加载

友情提醒:
若是不想使用统一调用格式,可自定义Controller在自定义Controller中
仍可使用自动注入注入其它模块的服务并使用.

4.原理概述:

4.1.在消费者模块解析json文件使用Spring提供的hook BeanDefinitionRegistryPostProcessor
进行自定义bean及其注册,由于消费者只依赖api模块,所以只能拿到接口,自定义bean时使用自定义FactoryBean
进行生产,生产出来的bean会被注册到Spring容器,Spring容器再进行自动注入;

4.2.在生产者模块为含有@RemoteService注解的服务生成代理,当接受到/common/innerInvoke的请求时,找到该代理bean
进行业务处理并返还给消费者.

5.包路径解释:

annotations 远程服务调用注解

base        请求响应基类及响应枚举值

exceptions  自定义异常

invoke      核心逻辑处理,包含bean的生成、注册、代理

utils       工具类





