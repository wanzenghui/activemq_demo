---
typora-root-url: assets
typora-copy-images-to: assets
---



# 一、简介

1、异步处理：发送和接受是异步的

2、系统解耦：消息队列，发布pub/订阅sub模式下【发送后就结束不需要同步等待处理】

3、消息的消费顺序

4、MQ集群

5、丢到 /opt目录

6、解压启动

![image-20200723114739659](/image-20200723114739659.png)

7、安装jdk，配置环境变量 vim /etc/profile

```xml
export JAVA_HOME=/home/jmeapp/soft/jdk1.7.0_79
export JRE_HOME=${JAVA_HOME}/jre
export CLASSPATH=.:${JAVA_HOME}/lib:${JRE_HOME}/lib
export PATH=${JAVA_HOME}/bin:${JRE_HOME}/bin:$PATH
```



8、启动 bin   ./activemq start		关闭：./activemq stop

9、activemq默认端口61616，前端管理端口8161，localhost:8161【默认admin，admin登录】

netstat -anpt：查看所有启动的端口

netstat -anp|grep 61616：查看端口是否被占用

lsof -i:61616：查看端口是否被占用

ps -ef|grep activemq：查看进程是否启动

进程号：2024

![image-20200723133947219](/image-20200723133947219.png)

ps -ef|grep activemq|grep -v grep



9、带日志启动 在bin下   ./activemq start > /opt/apache-activemq-5.16.0/run_activemq.log		关闭：./activemq stop

10、关防火墙service iptables stop

11、java code需要的jar包：activemq-all.jar



12、jms规范：按照这个规范做所有mq的编码

目的地Destination：队列 or 主题

![image-20200723140252583](/image-20200723140252583.png)

点对点：目的地是队列queue

发布/订阅：目的地是主题topic

![image-20200723140517577](/image-20200723140517577.png)









![image-20200723142402203](/image-20200723142402203.png)

---

Topic的生产者消费者，就是改成了Topic就可以了

![image-20200723142822330](/image-20200723142822330.png)



消费者  监听器：

![image-20200723143805355](/image-20200723143805355.png)



![image-20200723143721570](/image-20200723143721570.png)









**远程debug**：

远程配置：去查看启动脚本

vi 查看这个启动脚本

![image-20200723183755757](/image-20200723183755757.png)

![image-20200723183639230](/image-20200723183639230.png)





# 二、springboot整合activemq

## 1、application.yml

activemq的地址端口

队列名字

主题名字

账号、密码

```yml
server:
  port: 7777

spring:
  activemq:
    broker-url: tcp://192.168.109.23:61616
    user: admin
    password: admin
    close-timeout: 15s   # 在考虑结束之前等待的时间
    in-memory: true      # 默认代理URL是否应该在内存中。如果指定了显式代理，则忽略此值。
    non-blocking-redelivery: false  # 是否在回滚回滚消息之前停止消息传递。这意味着当启用此命令时，消息顺序不会被保留。
    send-timeout: 0     # 等待消息发送响应的时间。设置为0等待永远。
    queue-name: active.queue
    topic-name: active.topic.name.model

  #  packages:
  #    trust-all: true #不配置此项，会报错
  pool:
    enabled: true
    max-connections: 10   #连接池最大连接数
    idle-timeout: 30000   #空闲的连接过期时间，默认为30秒

  # jms:
  #   pub-sub-domain: true  #默认情况下activemq提供的是queue模式，若要使用topic模式需要配置下面配置

# 是否信任所有包
#spring.activemq.packages.trust-all=
# 要信任的特定包的逗号分隔列表（当不信任所有包时）
#spring.activemq.packages.trusted=
# 当连接请求和池满时是否阻塞。设置false会抛“JMSException异常”。
#spring.activemq.pool.block-if-full=true
# 如果池仍然满，则在抛出异常前阻塞时间。
#spring.activemq.pool.block-if-full-timeout=-1ms
# 是否在启动时创建连接。可以在启动时用于加热池。
#spring.activemq.pool.create-connection-on-startup=true
# 是否用Pooledconnectionfactory代替普通的ConnectionFactory。
#spring.activemq.pool.enabled=false
# 连接过期超时。
#spring.activemq.pool.expiry-timeout=0ms
# 连接空闲超时
#spring.activemq.pool.idle-timeout=30s
# 连接池最大连接数
#spring.activemq.pool.max-connections=1
# 每个连接的有效会话的最大数目。
#spring.activemq.pool.maximum-active-session-per-connection=500
# 当有"JMSException"时尝试重新连接
#spring.activemq.pool.reconnect-on-exception=true
# 在空闲连接清除线程之间运行的时间。当为负数时，没有空闲连接驱逐线程运行。
#spring.activemq.pool.time-between-expiration-check=-1ms
# 是否只使用一个MessageProducer
#spring.activemq.pool.use-anonymous-producers=true
```

## 2、Application启动类

启动消息队列

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
@EnableJms    //启动消息队列
public class ActivemqdemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ActivemqdemoApplication.class, args);
    }

}
```



## 3、Configuration配置类

1、配置队列Bean或主题Bean，设置队列名return new ActiveMQQueue(queueName);  设置主题名return new ActiveMQTopic(topicName);

2、配置连接工厂ConnectionFactory的Bean，return new ActiveMQConnectionFactory(username, password, brokerUrl);

3、配置监听类@Bean("queueListener")、@Bean("topicListener")

```java
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.SimpleJmsListenerContainerFactory;
import org.springframework.jms.core.JmsMessagingTemplate;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.jms.Topic;

/**
 * 初始化和配置 ActiveMQ 的连接
 */
@Configuration
public class BeanConfig {

    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;

    @Value("${spring.activemq.user}")
    private String username;

    @Value("${spring.activemq.topic-name}")
    private String password;

    @Value("${spring.activemq.queue-name}")
    private String queueName;

    @Value("${spring.activemq.topic-name}")
    private String topicName;

    @Bean(name = "queue")
    public Queue queue() {
        return new ActiveMQQueue(queueName);
    }

    @Bean(name = "topic")
    public Topic topic() {
        return new ActiveMQTopic(topicName);
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        return new ActiveMQConnectionFactory(username, password, brokerUrl);
    }

    @Bean
    public JmsMessagingTemplate jmsMessageTemplate() {
        return new JmsMessagingTemplate(connectionFactory());
    }

    // 在Queue模式中，对消息的监听需要对containerFactory进行配置
    @Bean("queueListener")
    public JmsListenerContainerFactory<?> queueJmsListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleJmsListenerContainerFactory factory = new SimpleJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setPubSubDomain(false);
        return factory;
    }

    //在Topic模式中，对消息的监听需要对containerFactory进行配置
    @Bean("topicListener")
    public JmsListenerContainerFactory<?> topicJmsListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleJmsListenerContainerFactory factory = new SimpleJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setPubSubDomain(true);
        return factory;
    }
}
```

## 4、Producer生产者

创建生产者代码session.createProducer(topic)；

可以使用jmsMessagingTemplate代替：jmsMessagingTemplate.convertAndSend(destination, jsonString);并且不用指定topic，因为jmsMessagingTemplate已经与connectionFactory绑定了。所以要指定不同的生产者，就配置不同的jmsMessagingTemplate来发送消息就可以了。jmsMessagingTemplate.convertAndSend(destination, jsonString);



注：Queue和Topic都继承了Destination类

```java
import com.alibaba.fastjson.JSON;
import com.wan.activemqdemo.bean.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.Topic;

/**
 * 生产者（queue 和 topic）
 */
@RestController
public class ProducerController {
    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;

    @Autowired
    private Queue queue;

    @Autowired
    private Topic topic;

    @PostMapping("/queue/test")
    public String sendQueue(@RequestBody Message str) {
        sendMessage(queue, str);
        return "success";
    }

    @PostMapping("/topic/test")
    public String sendTopic(@RequestBody Message str) {
        sendMessage(topic, str);
        return "success";
    }

    // 发送消息，destination是发送到的队列，message是待发送的消息
    private void sendMessage(Destination destination, Message message) {
        String jsonString = JSON.toJSONString(message);
        jmsMessagingTemplate.convertAndSend(destination, jsonString);
    }
}
```

