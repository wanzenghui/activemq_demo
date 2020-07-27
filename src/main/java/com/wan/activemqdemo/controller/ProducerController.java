package com.wan.activemqdemo.controller;

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
