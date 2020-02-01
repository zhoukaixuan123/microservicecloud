##  服务搭建

  microservicecloud-api  bean
  microservicecloud-provider-dept-8001   服务提供
  microservicecloud-consumer-dept-80  消费者
  
### Eureka是什么
 

```
Eureka是Netflix的一个子模块，也是核心模块之一。Eureka是一个基于REST的服务，用于定位服务，以实现云端中间层服务发现和故障转移。


Eureka是Netflix的一个子模块，也是核心模块之一。Eureka是一个基于REST的服务，用于定位服务，以实现云端中间层服务发现和故障转移。
```



## 基本架构

 Spring Cloud 封装了 Netflix 公司开发的 Eureka 模块来实现服务注册和发现(请对比Zookeeper)。
 
Eureka 采用了 C-S 的设计架构。Eureka Server 作为服务注册功能的服务器，它是服务注册中心。
 
而系统中的其他微服务，使用 Eureka 的客户端连接到 Eureka Server并维持心跳连接。这样系统的维护人员就可以通过 Eureka Server 来监控系统中各个微服务是否正常运行。SpringCloud 的一些其他模块（比如Zuul）就可以通过 Eureka Server 来发现系统中的其他微服务，并执行相关的逻辑。


## Eureka包含两个组件：Eureka Server和Eureka Client
Eureka Server提供服务注册服务
各个节点启动后，会在EurekaServer中进行注册，这样EurekaServer中的服务注册表中将会存储所有可用服务节点的信息，服务节点的信息可以在界面中直观的看到
 
 
EurekaClient是一个Java客户端，用于简化Eureka Server的交互，客户端同时也具备一个内置的、使用轮询(round-robin)负载算法的负载均衡器。在应用启动后，将会向Eureka Server发送心跳(默认周期为30秒)。如果Eureka Server在多个心跳周期内没有接收到某个节点的心跳，EurekaServer将会从服务注册表中把这个服务节点移除（默认90秒）


## eureka服务配置文件


```

server:
  port: 7001

eureka:
  instance:
    hostname: localhost #eureka服务端的实例名称
  client:
    register-with-eureka: false #false表示不向注册中心注册自己。
    fetch-registry: false #false表示自己端就是注册中心，我的职责就是维护服务实例，并不需要去检索服务
    service-url:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/        #设置与Eureka Server交互的地址查询服务和注册服务都需要依赖这个地址。



```

### 注册到eureka服配置

   pom
   
      <!--注册eureka-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-eureka</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>
    
    yml
    
    
    eureka:
      client: #客户端注册进eureka服务列表内
        service-url:
          defaultZone: http://localhost:7001/eureka
    
   
    启动类上加注解
    
    @EnableEurekaClient //本服务启动后会自动注册进
###  修改eureka注册中心的别名和显示IP
    
     eureka:
      client: #客户端注册进eureka服务列表内
        service-url:
          defaultZone: http://localhost:7001/eureka
      instance:   #默认服务名称修改
        instance-id: microservicecloud-dept8001
         prefer-ip-address: true  #访问路径可以显示IP地址
    
    
    
###  修改注册中心点击服务名字 找不到页面配置

    1. 个体服务添加监控
    <!-- actuator监控信息完善 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
    2. 父工程添加构建
         <build>
        <finalName>microservicecloud</finalName>
        <resources>
            <resource><!--读取所有moduel 中进行过滤，找到所有以$开头的配置 -->
                <directory>src/main/resources</directory>
                <filtering>true</filtering>
            </resource>
        </resources>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-resources-plugin</artifactId>
                <configuration>
                    <delimiters>
                        <delimit>$</delimit>
                    </delimiters>
                </configuration>
            </plugin>
        </plugins>
    </build>
    
    3. 个体服务添加描述信息
    
      info:
          app.name: atguigu-microservicecloud
          company.name: www.atguigu.com
          build.artifactId: $project.artifactId$
          build.version: $project.version$
    

    
### 什么是自我保护模式？
  
    默认情况下，如果EurekaServer在一定时间内没有接收到某个微服务实例的心跳，EurekaServer将会注销该实例（默认90秒）。但是当网络分区故障发生时，微服务与EurekaServer之间无法正常通信，以上行为可能变得非常危险了——因为微服务本身其实是健康的，此时本不应该注销这个微服务。Eureka通过“自我保护模式”来解决这个问题——当EurekaServer节点在短时间内丢失过多客户端时（可能发生了网络分区故障），那么这个节点就会进入自我保护模式。一旦进入该模式，EurekaServer就会保护服务注册表中的信息，不再删除服务注册表中的数据（也就是不会注销任何微服务）。当网络故障恢复后，该Eureka Server节点会自动退出自我保护模式。
     
    在自我保护模式中，Eureka Server会保护服务注册表中的信息，不再注销任何服务实例。当它收到的心跳数重新恢复到阈值以上时，该Eureka Server节点就会自动退出自我保护模式。它的设计哲学就是宁可保留错误的服务注册信息，也不盲目注销任何可能健康的服务实例。一句话讲解：好死不如赖活着
     
    综上，自我保护模式是一种应对网络异常的安全保护措施。它的架构哲学是宁可同时保留所有微服务（健康的微服务和不健康的微服务都会保留），也不盲目注销任何健康的微服务。使用自我保护模式，可以让Eureka集群更加的健壮、稳定。
     
    在Spring Cloud中，可以使用eureka.server.enable-self-preservation = false 禁用自我保护模式。   
    
    
    
### 关闭自我保护机制


```
eureka.server.enable-self-preservation = false 
```


### 服务发现
   
  服务端
```
 1.
     @Autowired
     private DiscoveryClient client;
  
  2.
   @RequestMapping(value = "/dept/discovery",method = RequestMethod.GET)
  public Object discovery(){
    List<String> list = client.getServices();
    System.out.println("*******************"+ list);

    List<ServiceInstance> srcList = client.getInstances("MICROSERVICECLOUD-DEPT");
    for (ServiceInstance element : srcList) {
      System.out.println(element.getServiceId()+"\t"+element
      .getHost()+"\t"+element.getPort()+"\t"+element.getUri());
    }
    return this.client;
  }
```
  消费端
  
```
 @RequestMapping(value = "/consumer/dept/discovery")
    public Object discovery(){
        return restTemplate.getForObject(REST_URL_PREFIX+"/dept/discovery", Object.class);
    }
```
###  集群配置


   添加多个注册中心相互注册
   
  eureka 注册服务 
   
```
eureka:
  instance:
    hostname: eureka7003.com #eureka服务端的实例名称
  client:
    register-with-eureka: false     #false表示不向注册中心注册自己。
    fetch-registry: false     #false表示自己端就是注册中心，我的职责就是维护服务实例，并不需要去检索服务
    service-url:
      #defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/       #设置与Eureka Server交互的地址查询服务和注册服务都需要依赖这个地址。
      defaultZone: http://eureka7001.com:7001/eureka/,http://eureka7004.com:7004/eureka/

```


 eureka客户端
 
 
```
eureka:
  client: #客户端注册进eureka服务列表内
    service-url:
      defaultZone: http://eureka7001.com:7001/eureka/,http://eureka7002.com:7002/eureka/,http://eureka7003.com:7003/eureka/
  instance:
    instance-id: microservicecloud-dept8001   #自定义服务名称信息
    prefer-ip-address: true     #访问路径可以显示IP地址

```
### Ribbon是什么

```
Spring Cloud Ribbon是基于Netflix Ribbon实现的一套客户端       负载均衡的工具。
 
简单的说，Ribbon是Netflix发布的开源项目，主要功能是提供客户端的软件负载均衡算法，将Netflix的中间层服务连接在一起。Ribbon客户端组件提供一系列完善的配置项如连接超时，重试等。简单的说，就是在配置文件中列出Load Balancer（简称LB）后面所有的机器，Ribbon会自动的帮助你基于某种规则（如简单轮询，随机连接等）去连接这些机器。我们也很容易使用Ribbon实现自定义的负载均衡算法。
```
## 可以做什么

```
LB，即负载均衡(Load Balance)，在微服务或分布式集群中经常用的一种应用。
负载均衡简单的说就是将用户的请求平摊的分配到多个服务上，从而达到系统的HA。
常见的负载均衡有软件Nginx，LVS，硬件 F5等。
相应的在中间件，例如：dubbo和SpringCloud中均给我们提供了负载均衡，SpringCloud的负载均衡算法可以自定义。 
```

## （硬件负载）集中式LB

      即在服务的消费方和提供方之间使用独立的LB设施(可以是硬件，如F5, 也可以是软件，如nginx), 由该设施负责把访问请求通过某种策略转发至服务的提供方；
  
## （软件负载）进程内LB
 
    将LB逻辑集成到消费方，消费方从服务注册中心获知有哪些地址可用，然后自己再从这些地址中选择出一个合适的服务器。
     
    Ribbon就属于进程内LB，它只是一个类库，集成于消费方进程，消费方通过它来获取到服务提供方的地址。  

### 资料地址  https://github.com/Netflix/ribbon/wiki/Getting-Started


###  Ribbon 需要和eureka客户端整合

## Ribbon 使用


```
 注册RestTemplate 调用对象的时候，就对RestTemplate进行负载
    @Bean
    @LoadBalanced
    public RestTemplate 调用对象的时候， getRestTemplate()
    {
         return new RestTemplate();
    }
    
    @Bean  // 自定义策略
    public IRule myRule(){
        return  new RandomRule();
    }
```
### Ribbon 算法
 
    默认算法有7种
    默认算法-轮询算法
    1. RoundRobinRule   - 轮询算法
    2.RandomRule----随机
    3.AvailabilityFilteringRule----会先过滤掉由于多次访问故障而处于断路器跳闸状态的服务，还有并发的连接数量超过阈值的服务，然后对剩余的服务列表按照轮询策略进行访问
    4.WeightedResponseTimeRule----根据平均响应时间计算所有服务的权重，响应时间越快服务权重越大被选中的概率越高。刚启动时如果统计信息不足，则使用RoundRobinRule策略，等统计信息足够，会切换到WeightedResponseTimeRule
    5、RetryRule----先按照RoundRobinRule的策略获取服务，如果获取服务失败则在指定时间内会进行重试，获取可用的服务
    6、BestAvailableRule----会先过滤掉由于多次访问故障而处于断路器跳闸状态的服务，然后选择一个并发量最小的服务
    7、ZoneAvoidanceRule----默认规则,复合判断server所在区域的性能和server的可用性选择服务器


    
    
## 自定义Ribbon算法   （核心组件IRule） 

    警告： 这个自定义配置类不能放在@CompinentSan所扫描的当前包以及子包下
          搜则我们自定义的这个配置类就会被所有的Ribbon客户端所共享，也就是说我们
          达不到特殊定制的目的了

```
     
    1. 启动类上加注解
    @RibbonClient(name="MICROSERVICECLOUD-DEPT",configuration= MySelfRule.class)

    2.（不能放在@CompinentSan所扫描的当前包以及子包下）
    
    @Configuration
    public class MySelfRule
    {
    	@Bean
    	public IRule myRule()
    	{
    		//return new RandomRule();// Ribbon默认是轮询，我自定义为随机
    		//return new RoundRobinRule();// Ribbon默认是轮询，我自定义为随机
    		
    		return new RandomRule_ZY();// 我自定义为每台机器5次
    	}
    }


  3.自定义类（不能放在@CompinentSan所扫描的当前包以及子包下）
  
  public class RandomRule_ZY extends AbstractLoadBalancerRule {
 
	private int total = 0; // 总共被调用的次数，目前要求每台被调用5次
	private int currentIndex = 0; // 当前提供服务的机器号
 
	public Server choose(ILoadBalancer lb, Object key) {
		if (lb == null) {
			return null;
		}
		Server server = null;
 
		while (server == null) {
			if (Thread.interrupted()) {
				return null;
			}
			List<Server> upList = lb.getReachableServers();
			List<Server> allList = lb.getAllServers();
 
			int serverCount = allList.size();
			if (serverCount == 0) {
				return null;
			}
 
			if (total < 5) {
				server = upList.get(currentIndex);
				total++;
			} else {
				total = 0;
				currentIndex++;
				if (currentIndex >= upList.size()) {
					currentIndex = 0;
				}
			}
 
			if (server == null) {
				Thread.yield();
				continue;
			}
 
			if (server.isAlive()) {
				return (server);
			}
 
			server = null;
			Thread.yield();
		}
 
		return server;
 
	}
 
	@Override
	public Server choose(Object key) {
		return choose(getLoadBalancer(), key);
	}
 
	@Override
	public void initWithNiwsConfig(IClientConfig arg0) {
	}
 
}

```

## 基础知识
    官网解释：
    http://projects.spring.io/spring-cloud/spring-cloud.html#spring-cloud-feign
     
     Feign是一个声明式WebService客户端。使用Feign能让编写Web Service客户端更加简单, 它的使用方法是定义一个接口，然后在上面添加注解，同时也支持JAX-RS标准的注解。Feign也支持可拔插式的编码器和解码器。Spring Cloud对Feign进行了封装，使其支持了Spring MVC标准注解和HttpMessageConverters。Feign可以与Eureka和Ribbon组合使用以支持负载均衡。
     ；
     
     Feign是一个声明式的Web服务客户端，使得编写Web服务客户端变得非常容易，
    只需要创建一个接口，然后在上面添加注解即可。
    参考官网：https://github.com/OpenFeign/feign 
 
###  Feign能干什么
    Feign旨在使编写Java Http客户端变得更容易。
    前面在使用Ribbon+RestTemplate时，利用RestTemplate对http请求的封装处理，形成了一套模版化的调用方法。但是在实际开发中，由于对服务依赖的调用可能不止一处，往往一个接口会被多处调用，所以通常都会针对每个微服务自行封装一些客户端类来包装这些依赖服务的调用。所以，Feign在此基础上做了进一步封装，由他来帮助我们定义和实现依赖服务接口的定义。在Feign的实现下，我们只需创建一个接口并使用注解的方式来配置它(以前是Dao接口上面标注Mapper注解,现在是一个微服务接口上面标注一个Feign注解即可)，即可完成对服务提供方的接口绑定，简化了使用Spring cloud Ribbon时，自动封装服务调用客户端的开发量。
 
## Feign集成了Ribbon
    利用Ribbon维护了MicroServiceCloud-Dept的服务列表信息，并且通过轮询实现了客户端的负载均衡。而与Ribbon不同的是，通过feign只需要定义服务绑定接口且以声明式的方法，优雅而简单的实现了服务调用
    
    
    
###  使用

  
```
    1. 依赖
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-feign</artifactId>
        </dependency>
    2. 创建调用接口
       package com.atguigu.springcloud.service;
         
        import java.util.List;
         
        import org.springframework.cloud.netflix.feign.FeignClient;
        import org.springframework.web.bind.annotation.PathVariable;
        import org.springframework.web.bind.annotation.RequestMapping;
        import org.springframework.web.bind.annotation.RequestMethod;
         
        import com.atguigu.springcloud.entities.Dept;
         
        @FeignClient(value = "MICROSERVICECLOUD-DEPT")
        public interface DeptClientService
        {
          @RequestMapping(value = "/dept/get/{id}",method = RequestMethod.GET)
          public Dept get(@PathVariable("id") long id);
         
          @RequestMapping(value = "/dept/list",method = RequestMethod.GET)
          public List<Dept> list();
         
          @RequestMapping(value = "/dept/add",method = RequestMethod.POST)
          public boolean add(Dept dept);
        }
         
     3. 调用的时候就用这个Service调用
        @RestController
        public class DeptController_Feign
        {
            @Autowired
            private DeptClientService service = null;
        
            @RequestMapping(value = "/consumer/dept/get/{id}")
            public Dept get(@PathVariable("id") Long id)
            {
                return this.service.get(id);
            }
        
            @RequestMapping(value = "/consumer/dept/list")
            public List<Dept> list()
            {
                return this.service.list();
            }
        
            @RequestMapping(value = "/consumer/dept/add")
            public Object add(Dept dept)
            {
                return this.service.add(dept);
            }
        }
        
    4. 启动类添加注解
     @EnableFeignClients(basePackages= {"com.atguigu.springcloud"})
     @ComponentScan("com.atguigu.springcloud")

        
```
 ### 分布式面临的问题
 
 
 ```
 服务雪崩
 多个微服务之间调用的时候，假设微服务A调用微服务B和微服务C，微服务B和微服务C又调用其它的微服务，这就是所谓的“扇出”。如果扇出的链路上某个微服务的调用响应时间过长或者不可用，对微服务A的调用就会占用越来越多的系统资源，进而引起系统崩溃，所谓的“雪崩效应”.
  
 对于高流量的应用来说，单一的后端依赖可能会导致所有服务器上的所有资源都在几秒钟内饱和。比失败更糟糕的是，这些应用程序还可能导致服务之间的延迟增加，备份队列，线程和其他系统资源紧张，导致整个系统发生更多的级联故障。这些都表示需要对故障和延迟进行隔离和管理，以便单个依赖关系的失败，不能取消整个应用程序或系统。
 ```
 
 
 ## Hystrix是什么
 
 ```
 Hystrix是一个用于处理分布式系统的延迟和容错的开源库，在分布式系统里，许多依赖不可避免的会调用失败，比如超时、异常等，Hystrix能够保证在一个依赖出问题的情况下，不会导致整体服务失败，避免级联故障，以提高分布式系统的弹性。
  
 “断路器”本身是一种开关装置，当某个服务单元发生故障之后，通过断路器的故障监控（类似熔断保险丝），向调用方返回一个符合预期的、可处理的备选响应（FallBack），而不是长时间的等待或者抛出调用方无法处理的异常，这样就保证了服务调用方的线程不会被长时间、不必要地占用，从而避免了故障在分布式系统中的蔓延，乃至雪崩。
 ```
 ## Hystrix 的作用
 
      服务降级
      服务熔断
      服务限流
      接近实时的监控
      等
      
 ### 官网资料 https://github.com/Netflix/Hystrix/wiki/How-To-Use     
 
 
 
 ### 什么是服务熔断
 
 ```
 服务熔断
 熔断机制是应对雪崩效应的一种微服务链路保护机制。
 当扇出链路的某个微服务不可用或者响应时间太长时，会进行服务的降级，进而熔断该节点微服务的调用，快速返回"错误"的响应信息。当检测到该节点微服务调用响应正常后恢复调用链路。在SpringCloud框架里熔断机制通过Hystrix实现。Hystrix会监控微服务间调用的状况，当失败的调用到一定阈值，缺省是5秒内20次调用失败就会启动熔断机制。熔断机制的注解是@HystrixCommand。
 ```
 #### 服务熔断 使用
 
 
 ```
    1. 依赖
     <!--  hystrix -->
         <dependency>
             <groupId>org.springframework.cloud</groupId>
             <artifactId>spring-cloud-starter-hystrix</artifactId>
         </dependency>
     2. 使用注解（也可以在类上使用）以及方法
     
     
       @RequestMapping(value="/dept/get/{id}",method=RequestMethod.GET)
       @HystrixCommand(fallbackMethod = "processHystrix_Get")
       public Dept get(@PathVariable("id") Long id)
       {
         Dept dept =  this.service.get(id);
         if(null == dept)
         {
           throw new RuntimeException("该ID："+id+"没有没有对应的信息");
         }
         return dept;
       }
     
       public Dept processHystrix_Get(@PathVariable("id") Long id)
       {
         return new Dept().setDeptno(id)
                 .setDname("该ID："+id+"没有没有对应的信息,null--@HystrixCommand")
                 .setDb_source("no this database in MySQL");
       }
       
     3.启动类注解  @EnableCircuitBreaker  
 ```
 
 
 
 ## 什么是服务降级
 
 
 ```
 
 整体资源快不够了，忍痛将某些服务先关掉，待渡过难关，再开启回来。
 服务降级处理是在客户端实现完成的，与服务端没有关系
 ```
 
 
 
 ###  服务降级 使用
 
    maven依赖必不可少
 
 ```
 1. 创建服务降级实现类
    @Component//不要忘记添加，不要忘记添加
 public class DeptClientServiceFallbackFactory implements FallbackFactory<DeptClientService>
 {
   @Override
   public DeptClientService create(Throwable throwable)
   {
    return new DeptClientService() {
      @Override
      public Dept get(long id)
      {
        return new Dept().setDeptno(id)
                .setDname("该ID："+id+"没有没有对应的信息,Consumer客户端提供的降级信息,此刻服务Provider已经关闭")
                .setDb_source("no this database in MySQL");
      }
  
      @Override
      public List<Dept> list()
      {
        return null;
      }
  
      @Override
      public boolean add(Dept dept)
      {
        return false;
      }
    };
   }
 }
 
 
 2. 远程调用接口需要  使用下面注解进行指定
 @FeignClient(value = "MICROSERVICECLOUD-DEPT",fallbackFactory=DeptClientServiceFallbackFactory.class)
 
 ```
### 分布式面临的问题


```
服务雪崩
多个微服务之间调用的时候，假设微服务A调用微服务B和微服务C，微服务B和微服务C又调用其它的微服务，这就是所谓的“扇出”。如果扇出的链路上某个微服务的调用响应时间过长或者不可用，对微服务A的调用就会占用越来越多的系统资源，进而引起系统崩溃，所谓的“雪崩效应”.
 
对于高流量的应用来说，单一的后端依赖可能会导致所有服务器上的所有资源都在几秒钟内饱和。比失败更糟糕的是，这些应用程序还可能导致服务之间的延迟增加，备份队列，线程和其他系统资源紧张，导致整个系统发生更多的级联故障。这些都表示需要对故障和延迟进行隔离和管理，以便单个依赖关系的失败，不能取消整个应用程序或系统。
```


## Hystrix是什么

```
Hystrix是一个用于处理分布式系统的延迟和容错的开源库，在分布式系统里，许多依赖不可避免的会调用失败，比如超时、异常等，Hystrix能够保证在一个依赖出问题的情况下，不会导致整体服务失败，避免级联故障，以提高分布式系统的弹性。
 
“断路器”本身是一种开关装置，当某个服务单元发生故障之后，通过断路器的故障监控（类似熔断保险丝），向调用方返回一个符合预期的、可处理的备选响应（FallBack），而不是长时间的等待或者抛出调用方无法处理的异常，这样就保证了服务调用方的线程不会被长时间、不必要地占用，从而避免了故障在分布式系统中的蔓延，乃至雪崩。
```
## Hystrix 的作用

     服务降级
     服务熔断
     服务限流
     接近实时的监控
     等
     
### 官网资料 https://github.com/Netflix/Hystrix/wiki/How-To-Use     



### 什么是服务熔断

```
服务熔断
熔断机制是应对雪崩效应的一种微服务链路保护机制。
当扇出链路的某个微服务不可用或者响应时间太长时，会进行服务的降级，进而熔断该节点微服务的调用，快速返回"错误"的响应信息。当检测到该节点微服务调用响应正常后恢复调用链路。在SpringCloud框架里熔断机制通过Hystrix实现。Hystrix会监控微服务间调用的状况，当失败的调用到一定阈值，缺省是5秒内20次调用失败就会启动熔断机制。熔断机制的注解是@HystrixCommand。
```
#### 服务熔断 使用


```
   1. 依赖
    <!--  hystrix -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-hystrix</artifactId>
        </dependency>
    2. 使用注解（也可以在类上使用）以及方法
    
    
      @RequestMapping(value="/dept/get/{id}",method=RequestMethod.GET)
      @HystrixCommand(fallbackMethod = "processHystrix_Get")
      public Dept get(@PathVariable("id") Long id)
      {
        Dept dept =  this.service.get(id);
        if(null == dept)
        {
          throw new RuntimeException("该ID："+id+"没有没有对应的信息");
        }
        return dept;
      }
    
      public Dept processHystrix_Get(@PathVariable("id") Long id)
      {
        return new Dept().setDeptno(id)
                .setDname("该ID："+id+"没有没有对应的信息,null--@HystrixCommand")
                .setDb_source("no this database in MySQL");
      }
      
    3.启动类注解  @EnableCircuitBreaker  
```



## 什么是服务降级


```

整体资源快不够了，忍痛将某些服务先关掉，待渡过难关，再开启回来。
服务降级处理是在客户端实现完成的，与服务端没有关系
```



###  服务降级 使用

   maven依赖必不可少

```
1. 创建服务降级实现类
   @Component//不要忘记添加，不要忘记添加
public class DeptClientServiceFallbackFactory implements FallbackFactory<DeptClientService>
{
  @Override
  public DeptClientService create(Throwable throwable)
  {
   return new DeptClientService() {
     @Override
     public Dept get(long id)
     {
       return new Dept().setDeptno(id)
               .setDname("该ID："+id+"没有没有对应的信息,Consumer客户端提供的降级信息,此刻服务Provider已经关闭")
               .setDb_source("no this database in MySQL");
     }
 
     @Override
     public List<Dept> list()
     {
       return null;
     }
 
     @Override
     public boolean add(Dept dept)
     {
       return false;
     }
   };
  }
}


2. 远程调用接口需要  使用下面注解进行指定
@FeignClient(value = "MICROSERVICECLOUD-DEPT",fallbackFactory=DeptClientServiceFallbackFactory.class)

```


### 服务监控hystrixDashboard

```
除了隔离依赖服务的调用以外，Hystrix还提供了准实时的调用监控（Hystrix Dashboard），Hystrix会持续地记录所有通过Hystrix发起的请求的执行信息，
并以统计报表和图形的形式展示给用户，包括每秒执行多少请求多少成功，多少失败等。
Netflix通过hystrix-metrics-event-stream项目实现了对以上指标的监控。Spring Cloud也提供了Hystrix Dashboard的整合，对监控内容转化成可视化界面。
```


#### 服务监控hystrixDashboard 使用


```
1. 创建监控服务
2.依赖
<!-- hystrix和 hystrix-dashboard相关-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-hystrix</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-hystrix-dashboard</artifactId>
        </dependency>
3.启动类 注解 @EnableHystrixDashboard
4.所有的服务模块引入监控依赖
<!-- actuator监控信息完善 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
5.监控访问路径  http://localhost:9001/hystrix     

```
## 6. 图形化页面 识别
![image](https://note.youdao.com/yws/api/personal/file/DC8D7F8FA6174A5292789F2EAA153D5B?method=download&shareKey=98d8bf584427df538d76ed6c2c74482a)
```
1.实心圆：共有两种含义。它通过颜色的变化代表了实例的健康程度，它的健康度从绿色<黄色<橙色<红色递减。
该实心圆除了颜色的变化之外，它的大小也会根据实例的请求流量发生变化，流量越大该实心圆就越大。所以通过该实心圆的展示，就可以在大量的实例中快速的发现故障实例和高压力实例。
2.曲线：用来记录2分钟内流量的相对变化，可以通过它来观察到流量的上升和下降趋势。
```
![image](https://note.youdao.com/yws/api/personal/file/49EFDEEF57094574ACAE3F9D2FDFB127?method=download&shareKey=bfb7c1bf71a376963279366420b51f77)


###  zuul是什么


```
Zuul包含了对请求的路由和过滤两个最主要的功能：
其中路由功能负责将外部请求转发到具体的微服务实例上，是实现外部访问统一入口的基础而过滤器功能则负责对请求的处理过程进行干预，是实现请求校验、服务聚合等功能的基础.
 
Zuul和Eureka进行整合，将Zuul自身注册为Eureka服务治理下的应用，同时从Eureka中获得其他微服务的消息，也即以后的访问微服务都是通过Zuul跳转后获得。
 
    注意：Zuul服务最终还是会注册进Eureka
 
提供=代理+路由+过滤三大功能
```



###   能干什么

    路由
    过滤
    
    
## 官网资料  https://github.com/Netflix/zuul/wiki/Getting-Started    



###  使用


```
1、依赖

     <dependency>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-starter-eureka</artifactId>
   </dependency>
   <dependency>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-starter-zuul</artifactId>
   </dependency>
2.yml 配置
    server:
      port: 9527

    spring:
      application:
        name: microservicecloud-zuul-gateway
    
    eureka:
      client:
        service-url:
          defaultZone: http://eureka7001.com:7001/eureka,http://eureka7002.com:7002/eureka,http://eureka7003.com:7003/eureka
      instance:
        instance-id: gateway-9527.com
        prefer-ip-address: true
    
    
    info:
      app.name: atguigu-microcloud
      company.name: www.atguigu.com
      build.artifactId: $project.artifactId$
      build.version: $project.version$
    
    zuul:
      ignored-services: microservicecloud-dept  服务不能通过这个访问
      routes:
        mydept.serviceId: microservicecloud-dept   把这个服务
        mydept.path: /mydept/**   替换成这个服务映射
3.启动类注解
   @EnableZuulProxy

```
### zuul配置讲解

```
 
    zuul:
      prefix: /atguigu  # 设置公共的前缀
      ignored-services: microservicecloud-dept  服务不能通过这个访问（不能使用服务名进行调用，只能用以下设置。批量处理用"*"）
      routes:
        mydept.serviceId: microservicecloud-dept   把这个服务
        mydept.path: /mydept/**
        
        
    访问路径案例：http://myzuul.com:9527/atguigu/mydept/dept/get/1    
```
###  分布式系统面临为题


```
  微服务意味着将单体应用中的业务拆分成一个个子服务，每个服务的粒度相对较小，因此系统中出现大量的服务。由于服务都需要必要的配置信息才能运行，所以一套集中式的，动态的配置管理设施是比不过少的。springcloud提供了ConfigServer来解决这个问题。
```

## 什么是分布式配置中心


```
  springcloud Config 为微服务架构中的微服务提供集中化的外部配置支持，配置服务为各个不同微服务应用的所有环境提供了一个中心化的外部配置。
  
  
  springcloud Config分为服务端和客户端两部分
  
  服务端也称为分布式配置中心，他是一个独立的微服务应用，用来连接配置服务器并为客户端提供获取配置信息，加密/解密等访问接口。
  
  
  客户端则通过制定的配置中心来管理应用资源，以及与业务相关的配置内容，并在启动的时候从配置中心获取和加载配置信息配置服务器默认采用git来存储配置信息，这样就有助于对环境进行版本管理，并且可以通过git客户端工具来方便的管理和访问配置内容。
```


###   可以解决的问题？


```
1.集中管理配置文件
2.不同环境不同配置，动态化的配置更新，分环境部署比如dev/test/prod/beta
3.运行期间动态调整配置，不在需要在每个服务部署的机器上编写配置文件，服务会向配置中心统一来去配置
4.当配置发生变动时，服务部不需要重启即可感知到配置的变化并应用新的配置
5 降配置信息以Rest接口的方式暴露
```

## 与github整合配置  从github上读取配置信息 

 
```
1、在github上创建一个仓库上面创建一个application.yml  配置部署信息
如：
    spring:
      profiles:
        active: 
          - dev
    ---      
    spring:
       profiles: dev #开发环境
       application:
            name: microservicecloud-config-atguigu-dev   
    ---
    spring:
       profiles: test #测试环境
       application:
            name: microservicecloud-config-atguigu-test  
    # 请保存为UTF-8格式  
2、创建注册中心服务端
        2.1  依赖
            <!-- springCloud Config -->
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-config-server</artifactId>
		</dependency>
		<!-- 避免Config的Git插件报错：org/eclipse/jgit/api/TransportConfigCallback -->
		<dependency>
			<groupId>org.eclipse.jgit</groupId>
			<artifactId>org.eclipse.jgit</artifactId>
			<version>4.10.0.201712302008-r</version>
		</dependency>
		<!-- 图形化监控 -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-actuator</artifactId>
		</dependency>
		<!-- 熔断 -->
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-hystrix</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-eureka</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-config</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-jetty</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
		</dependency>
		<!-- 热部署插件 -->
		<dependency>
			<groupId>org.springframework</groupId>
			<artifactId>springloaded</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-devtools</artifactId>
		</dependency>
    	2.2 启动类注解 @EnableConfigServer
3.yml配置信息
    server: 
      port: 3344 
      
    spring:
      application:
        name:  microservicecloud-config
      cloud:
        config:
          server:
            git:
              uri: git@github.com:zzyybs/microservicecloud-config.git #GitHub上面的git仓库名字
 


```

### 读取配置规则

         http://config-3344.com:3344/application-test.yml  读取可视化
         http://config-3344.com:3344/application/dev/master  读取json串
         http://config-3344.com:3344/master/application   json串 
          读取的配置信息样式和访问路径有关
          
          
###  配置中心创建客户端



```
1、依赖
<dependencies>
		<!-- SpringCloud Config客户端 -->
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-config</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-actuator</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-hystrix</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-eureka</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-config</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-jetty</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework</groupId>
			<artifactId>springloaded</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-devtools</artifactId>
		</dependency>
	</dependencies>
	
2.bootstrap.yml 配置文件配置
    spring:
      cloud:
        config:
          name: microservicecloud-config-client #需要从github上读取的资源名称，注意没有yml后缀名  （这个是github上的  读取的文件名字）
          profile: test   #本次访问的配置项   读什么这里设置什么（github配置文件中profile）
          label: master   
          uri: http://config-3344.com:3344  #本微服务启动后先去找3344号服务，通过SpringCloudConfig获取GitHub的服务地址
 

3.appliation.yml配置
spring:
  application:
    name: microservicecloud-config-client
    
4、获取github上的配置信息


        @RestController
        public class ConfigClientRest
        {
        
        	@Value("${spring.application.name}")
        	private String applicationName;
        
        	@Value("${eureka.client.service-url.defaultZone}")
        	private String eurekaServers;
        
        	@Value("${server.port}")
        	private String port;
        
        	@RequestMapping("/config")
        	public String getConfig()
        	{
        		String str = "applicationName: " + applicationName + "\t eurekaServers:" + eurekaServers + "\t port: " + port;
        		System.out.println("******str: " + str);
        		return "applicationName: " + applicationName + "\t eurekaServers:" + eurekaServers + "\t port: " + port;
        	}
        }

	
```

      