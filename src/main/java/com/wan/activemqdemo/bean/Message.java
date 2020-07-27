package com.wan.activemqdemo.bean;

import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class Message implements Serializable {

    private static final long serialVersionUID = 8683452581122892669L;

    private String aaa;

    public Message() {
    }

    public String getAaa() {
        return aaa;
    }

    public void setAaa(String aaa) {
        this.aaa = aaa;
    }

    @Override
    public String toString() {
        return "Message{" +
                "aaa='" + aaa + '\'' +
                '}';
    }
}
