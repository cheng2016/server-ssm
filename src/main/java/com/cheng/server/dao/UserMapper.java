package com.cheng.server.dao;

import com.cheng.server.entity.User;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMapper {
    User selectByPrimaryKey(String id);

    List<User> queryAll();
}
