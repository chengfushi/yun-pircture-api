package com.chengfu.yunpictureapi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chengfu.yunpictureapi.exception.BusinessException;
import com.chengfu.yunpictureapi.exception.ErrorCode;
import com.chengfu.yunpictureapi.exception.ThrowUtils;
import com.chengfu.yunpictureapi.model.dto.UserRegisterRequest;
import com.chengfu.yunpictureapi.model.entity.User;
import com.chengfu.yunpictureapi.model.enums.UserRoleEnum;
import com.chengfu.yunpictureapi.service.UserService;
import com.chengfu.yunpictureapi.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
* @author Lenovo
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-06-21 14:37:56
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Override
    public Long userRegister(String userAccount, String userPassword, String checkPassword) {


        ThrowUtils.throwIf(userAccount == null || userAccount.isEmpty(), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(userPassword == null || userPassword.isEmpty(), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(checkPassword == null || checkPassword.isEmpty(), ErrorCode.PARAMS_ERROR);

        //校验两次密码是否一致
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR);

        //校验用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        Long selectCount = this.baseMapper.selectCount(queryWrapper);
        if (selectCount > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已存在");
        }

        //创建用户对象
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(getEncryptPassword(userPassword));
        user.setUserName("智图云链默认用户");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean save = this.save(user);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户注册失败");
        }
        return user.getId();
    }


    @Override
    public String getEncryptPassword(String userPassword) {
        // 盐值，混淆密码
        final String SALT = "chengfu";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

}




