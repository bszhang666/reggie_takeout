package com.itheima.reggie.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("prototype")//多例
public class EmailTask implements Serializable {//实现串行化

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${reggie.email.system}")
    private String mailbox;//发件人邮箱

    @Async//异步执行注解
    public void sendAsync(SimpleMailMessage message){
        message.setFrom(mailbox);
        message.setCc(mailbox);//抄送给自己
        javaMailSender.send(message);
    }
}
