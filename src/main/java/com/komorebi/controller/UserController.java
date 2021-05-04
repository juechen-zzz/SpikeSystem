package com.komorebi.controller;

import com.komorebi.pojo.SpikeUser;
import com.komorebi.result.Result;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/user")
public class UserController {
    @RequestMapping("/info")
    @ResponseBody
    public Result<SpikeUser> info(Model model, SpikeUser user) {
        return Result.success(user);
    }
}
