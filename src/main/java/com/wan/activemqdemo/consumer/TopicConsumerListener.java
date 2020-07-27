package com.wan.activemqdemo.consumer;

import com.alibaba.fastjson.JSON;
import com.wan.activemqdemo.bean.Message;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * topic模式的消费者
 */
@Component
public class TopicConsumerListener {
    //topic模式的消费者
    @JmsListener(destination="${spring.activemq.topic-name}", containerFactory="topicListener")
    public void readActiveQueue(String message) {
        Message messageObj = JSON.parseObject(message, Message.class);
        System.out.println("topic接受到：" + messageObj);
    }
}
