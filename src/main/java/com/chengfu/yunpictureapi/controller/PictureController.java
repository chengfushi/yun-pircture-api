package com.chengfu.yunpictureapi.controller;

import cn.hutool.core.util.RandomUtil;
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
import com.chengfu.yunpictureapi.model.dto.picture.*;
import com.chengfu.yunpictureapi.model.entity.Picture;
import com.chengfu.yunpictureapi.model.entity.User;
import com.chengfu.yunpictureapi.model.enums.PictureReviewStatusEnum;
import com.chengfu.yunpictureapi.model.vo.picture.PictureTagCategory;
import com.chengfu.yunpictureapi.model.vo.picture.PictureVO;
import com.chengfu.yunpictureapi.service.PictureService;
import com.chengfu.yunpictureapi.service.UserService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private PictureService pictureService;

    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private final Cache<String, String> LOCAL_CACHE =
            Caffeine.newBuilder().initialCapacity(1024)
                    .maximumSize(10000L)
                    // 缓存 5 分钟移除
                    .expireAfterWrite(5L, TimeUnit.MINUTES)
                    .build();



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

    /**
     * 通过 URL 上传图片（可重新上传）
     */
    @PostMapping("/upload/url")
    public BaseResponse<PictureVO> uploadPictureByUrl(
            @RequestBody PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
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
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest,HttpServletRequest request) {
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

        //填充审核参数
        User loginUser = userService.getLoginUser(request);
        pictureService.fillReviewParams(picture, loginUser);
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
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 普通用户默认只能查看已过审的数据
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());

        // 构建缓存 key

        String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String cacheKey = "yupicture:listPictureVOByPage:" + hashKey;

        // 1. 查询本地缓存（Caffeine）
        String cachedValue = LOCAL_CACHE.getIfPresent(cacheKey);
        if (cachedValue != null) {
            Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(cachedPage);
        }

        // 2. 查询分布式缓存（Redis）
        ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
        cachedValue = valueOps.get(cacheKey);
        if (cachedValue != null) {
            // 如果命中 Redis，存入本地缓存并返回
            LOCAL_CACHE.put(cacheKey, cachedValue);
            Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(cachedPage);
        }

        // 3. 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, httpServletRequest);

        // 4. 更新缓存
        String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
        // 更新本地缓存
        LOCAL_CACHE.put(cacheKey, cacheValue);
        // 更新 Redis 缓存，设置过期时间为 5 - 10分钟
        valueOps.set(cacheKey, cacheValue, 5 + RandomUtil.randomInt(5), TimeUnit.MINUTES);


        // 返回结果
        return ResultUtils.success(pictureVOPage);
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

        pictureService.fillReviewParams(picture, loginUser);

        // 操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景","创意","头像");
        List<String> categoryList = Arrays.asList("壁纸", "动漫", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }

    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest,
                                                 HttpServletRequest request){
        ThrowUtils.throwIf(pictureReviewRequest == null || pictureReviewRequest.getId() == null,ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = pictureService.doPictureReview(pictureReviewRequest,loginUser);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR,"审核失败");
        return ResultUtils.success(result);
    }

    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(
            @RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,
            HttpServletRequest request
    ) {
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        int uploadCount = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
        return ResultUtils.success(uploadCount);
    }


}