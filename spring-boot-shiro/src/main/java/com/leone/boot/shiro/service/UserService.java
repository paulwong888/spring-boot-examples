package com.leone.boot.shiro.service;


import com.leone.boot.shiro.entity.User;

public interface UserService {

    User findByAccount(String username);

    User login(String account, String password);

    void logout(String account);

}