package com.komorebi.controller;

import com.komorebi.rabbitmq.MQSender;
import com.komorebi.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MQController {
    @Autowired
    MQSender sender;

    @RequestMapping("/mq")
    @ResponseBody
    public Result<String> mq() {
        sender.send("hello, rabbitmq direct");
        return Result.success("hello, world");
    }

    @RequestMapping("/mq/topic")
    @ResponseBody
    public Result<String> topic() {
        sender.sendTopic("hello, rabbitmq topic");
        return Result.success("hello, world");
    }

    @RequestMapping("/mq/fanout")
    @ResponseBody
    public Result<String> fanout() {
        sender.sendFanout("hello, rabbitmq fanout");
        return Result.success("hello, world");
    }

    @RequestMapping("/mq/header")
    @ResponseBody
    public Result<String> header() {
        sender.sendHeader("hello, rabbitmq header");
        return Result.success("hello, world");
    }
}
