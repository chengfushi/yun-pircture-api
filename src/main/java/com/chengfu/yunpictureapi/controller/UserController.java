package com.chengfu.yunpictureapi.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chengfu.yunpictureapi.annotation.AuthCheck;
import com.chengfu.yunpictureapi.common.BaseResponse;
import com.chengfu.yunpictureapi.common.DeleteRequest;
import com.chengfu.yunpictureapi.common.ResultUtils;
import com.chengfu.yunpictureapi.constant.UserConstant;
import com.chengfu.yunpictureapi.exception.BusinessException;
import com.chengfu.yunpictureapi.exception.ErrorCode;
import com.chengfu.yunpictureapi.exception.ThrowUtils;
import com.chengfu.yunpictureapi.model.dto.user.*;
import com.chengfu.yunpictureapi.model.entity.User;
import com.chengfu.yunpictureapi.model.vo.user.LoginUserVO;
import com.chengfu.yunpictureapi.model.vo.user.UserVO;
import com.chengfu.yunpictureapi.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        Long userRegister = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(userRegister);
    }

    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword,request);
        return ResultUtils.success(loginUserVO);
    }

    @GetMapping("/get/login")

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }

    @PostMapping("/logout")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean logout = userService.logout(request);
        return ResultUtils.success(logout);
    }

    /*
     * 创建用户
     * */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);

        //默认密码为12345678
        final String DEFAULT_PASSWORD = "12345678";
        user.setUserPassword(userService.getEncryptPassword(DEFAULT_PASSWORD));

        boolean save = userService.save(user);

        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户注册失败");
        }
        return ResultUtils.success(save);
    }

    /*
     * 删除用户
     * */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
        boolean remove = userService.removeById(deleteRequest.getId());
        if (!remove) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户删除失败");
        }
        return ResultUtils.success(remove);
    }

    /*
    根据id获取用户（管理员）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUser(long id) {
        ThrowUtils.throwIf(id < 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        return ResultUtils.success(user);
    }

    /*
     * 根据id获取包装类
     * */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVO(long id) {
        ThrowUtils.throwIf(id < 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        UserVO userVO = userService.getUserVO(user);
        return ResultUtils.success(userVO);
    }

    /*
     * 用户更新
     * */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throwIf(userUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean update = userService.updateById(user);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户更新失败");
        }
        return ResultUtils.success(update);
    }

    /*
     * 用户列表分页查询
     * */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> getUserVOListPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);

        int current = userQueryRequest.getCurrent();
        int pageSize = userQueryRequest.getPageSize();

        Page<User> userPage = userService.page(new Page<>(current, pageSize),
                userService.getQueryWrapper(userQueryRequest));

        Page<UserVO> userVOPage = new Page<>(current,pageSize,userPage.getTotal());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);

        return ResultUtils.success(userVOPage);
    }


}
