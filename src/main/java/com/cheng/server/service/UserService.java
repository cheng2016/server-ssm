package com.cheng.server.service;

import com.cheng.server.entity.User;

import java.util.List;

public interface UserService {
    User selectByPrimaryKey(String id);

     List<User> queryAll();
}
