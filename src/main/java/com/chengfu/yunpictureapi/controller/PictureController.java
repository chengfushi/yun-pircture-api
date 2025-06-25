package com.chengfu.yunpictureapi.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chengfu.yunpictureapi.annotation.AuthCheck;
import com.chengfu.yunpictureapi.common.BaseResponse;
import com.chengfu.yunpictureapi.common.DeleteRequest;
import com.chengfu.yunpictureapi.common.ResultUtils;
import com.chengfu.yunpictureapi.constant.UserConstant;
import com.chengfu.yunpictureapi.exception.BusinessException;
import com.chengfu.yunpictureapi.exception.ErrorCode;
import com.chengfu.yunpictureapi.exception.ThrowUtils;
import com.chengfu.yunpictureapi.model.dto.picture.PictureEditRequest;
import com.chengfu.yunpictureapi.model.dto.picture.PictureQueryRequest;
import com.chengfu.yunpictureapi.model.dto.picture.PictureUpdateRequest;
import com.chengfu.yunpictureapi.model.dto.picture.PictureUploadRequest;
import com.chengfu.yunpictureapi.model.entity.Picture;
import com.chengfu.yunpictureapi.model.entity.User;
import com.chengfu.yunpictureapi.model.vo.picture.PictureTagCategory;
import com.chengfu.yunpictureapi.model.vo.picture.PictureVO;
import com.chengfu.yunpictureapi.service.PictureService;
import com.chengfu.yunpictureapi.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@RestController
@Slf4j
@RequestMapping("/picture")
public class PictureController {
    @Resource
    private PictureService pictureService;
    @Resource
    private UserService userService;

    /**
     * 上传图片（可重新上传）
     */
    @PostMapping("/upload")
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile multipartFile,
                                                 PictureUploadRequest pictureUploadRequest,
                                                 HttpServletRequest httpServletRequest) {
        User user = userService.getLoginUser(httpServletRequest);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, user);

        return ResultUtils.success(pictureVO);
    }

    /*
     * 删除图片
     * */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest httpServletRequest) {
        //校验参数
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() < 0,
                ErrorCode.PARAMS_ERROR,
                "删除图片参数错误");
        //获取用户信息
        User user = userService.getLoginUser(httpServletRequest);

        //获取删除图片id
        Long pictureId = deleteRequest.getId();

        //判断是否存在
        Picture oldPicture = pictureService.getById(pictureId);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");

        //只允许管理员或本人删除图片
        ThrowUtils.throwIf(!Objects.equals(user.getId(), oldPicture.getUserId())
                && !UserConstant.ADMIN_ROLE.equals(user.getUserRole()), ErrorCode.NO_AUTH_ERROR, "无权删除图片");

        //删除图片
        boolean delete = pictureService.removeById(pictureId);
        ThrowUtils.throwIf(!delete, ErrorCode.SYSTEM_ERROR, "删除图片失败");
        return ResultUtils.success(delete);
    }

    /*
     * 更新图片，仅管理员可用
     * */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest) {
        //校验参数
        ThrowUtils.throwIf(pictureUpdateRequest == null || pictureUpdateRequest.getId() < 0,
                ErrorCode.PARAMS_ERROR,
                "更新图片参数不能为空");

        //将实体类和dto类进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateRequest, picture);
        //将json格式列表转为字符
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));

        //判断图片是否存在
        ThrowUtils.throwIf(pictureService.getById(picture.getId()) == null,
                ErrorCode.NOT_FOUND_ERROR,
                "图片不存在");
        //更新图片
        boolean update = pictureService.updateById(picture);
        ThrowUtils.throwIf(!update, ErrorCode.SYSTEM_ERROR, "更新图片失败");
        return ResultUtils.success(update);
    }

    /*
     *根据id获取图片（管理员可用）
     * */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPicture(Long id) {
        //校验参数
        ThrowUtils.throwIf(id == null || id < 0, ErrorCode.PARAMS_ERROR, "获取图片参数错误");

        //获取图片信息
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        return ResultUtils.success(picture);
    }

    /*
     * 根据id获取图片封装类
     * */
    @GetMapping("/get/vo")
    @AuthCheck
    public BaseResponse<PictureVO> getPictureVO(Long id, HttpServletRequest httpServletRequest) {
        //校验参数
        ThrowUtils.throwIf(id == null || id < 0, ErrorCode.PARAMS_ERROR, "获取图片参数错误");

        User user = userService.getLoginUser(httpServletRequest);
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");

        return ResultUtils.success(pictureService.getPictureVo(picture, httpServletRequest));
    }

    /**
     * 分页获取图片列表（仅管理员可用）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        long current = pictureQueryRequest.getCurrent();
        long page = pictureQueryRequest.getPageSize();
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current,page),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }
    /**
     * 分页获取图片包装列表
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                             HttpServletRequest httpServletRequest){
        long current = pictureQueryRequest.getCurrent();
        long page = pictureQueryRequest.getPageSize();

        //查询
        Page<Picture> picturePage = pictureService.page(new Page<>(current, page),
                pictureService.getQueryWrapper(pictureQueryRequest));
        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, httpServletRequest));
    }
    /**
     * 编辑图片（给用户使用）
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 在此处将实体类和 DTO 进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);
        // 注意将 list 转为 string
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        // 设置编辑时间
        picture.setEditTime(new Date());
        // 数据校验
        pictureService.validPicture(picture);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = pictureEditRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }


}