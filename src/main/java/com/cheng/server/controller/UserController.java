package com.cheng.server.controller;

import com.cheng.server.service.UserService;
import com.cheng.server.entity.User;
import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {
    private static final Log logger = LogFactory.getLog(UserController.class);

    @Resource
    private UserService userService;

    @RequestMapping(value = "/showUser",method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    String showUser() {
        List<User> list = this.userService.queryAll();
        return new Gson().toJson(list);
    }
}
