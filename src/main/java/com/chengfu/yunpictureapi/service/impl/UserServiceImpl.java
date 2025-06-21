package com.chengfu.yunpictureapi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chengfu.yunpictureapi.model.entity.User;
import com.chengfu.yunpictureapi.service.UserService;
import com.chengfu.yunpictureapi.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author Lenovo
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-06-21 14:37:56
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




