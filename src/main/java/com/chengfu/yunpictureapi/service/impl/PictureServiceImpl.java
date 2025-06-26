package com.chengfu.yunpictureapi.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chengfu.yunpictureapi.exception.BusinessException;
import com.chengfu.yunpictureapi.exception.ErrorCode;
import com.chengfu.yunpictureapi.exception.ThrowUtils;
import com.chengfu.yunpictureapi.manager.FileManager;
import com.chengfu.yunpictureapi.model.dto.picture.PictureQueryRequest;
import com.chengfu.yunpictureapi.model.dto.picture.PictureUploadRequest;
import com.chengfu.yunpictureapi.model.dto.file.UploadPictureResult;
import com.chengfu.yunpictureapi.model.entity.Picture;
import com.chengfu.yunpictureapi.model.entity.User;
import com.chengfu.yunpictureapi.model.vo.picture.PictureVO;
import com.chengfu.yunpictureapi.model.vo.user.UserVO;
import com.chengfu.yunpictureapi.service.PictureService;
import com.chengfu.yunpictureapi.mapper.PictureMapper;
import com.chengfu.yunpictureapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author Lenovo
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-06-23 11:35:45
*/
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{

    @Autowired
    FileManager fileManager;
    @Autowired
    private UserService userService;

    @Override
    public PictureVO uploadPicture(MultipartFile multipartFile,
                                   PictureUploadRequest pictureUploadRequest,
                                   User loginUser) {
        //抛出未登录异常
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        //判断是新增还是更新图片
        Long pictureId = null;

        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
        }

        //如果是更新图片，需要校验图片是否存在
        if (pictureId != null) {
            boolean exists = this.lambdaQuery()
                    .eq(Picture::getId, pictureId)
                    .exists();

            if (!exists) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片不存在");
            }
        }

        //按照用户id划分目录
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        //传图
        UploadPictureResult uploadPictureResult = fileManager.uploadPicture(multipartFile, uploadPathPrefix);

        //构造入库图片信息
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setName(uploadPictureResult.getPicName());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());

        //如果不是创建新图，需要更新图片id和编辑时间
        if (pictureId != null) {
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        boolean save = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "图片上传失败");

        return PictureVO.objToVo(picture);
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        //如果查询条件为空，直接返回
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }

        //从字段中取值
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        int current = pictureQueryRequest.getCurrent();
        int pageSize = pictureQueryRequest.getPageSize();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();

        //从多字段中搜素
        if (searchText != null) {
            queryWrapper.and(qw -> qw.like("name", searchText)
                    .or()
                    .like("introduction", searchText)
            );
        }

        //JSON数组查询
        if (tags != null && !tags.isEmpty()) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }

// 动态拼接非空条件
        if (id != null) {
            queryWrapper.eq("id", id);
        }
        if (userId != null) {
            queryWrapper.eq("userId", userId);
        }
        if (StrUtil.isNotBlank(name)) {
            queryWrapper.like("name", name);
        }
        if (StrUtil.isNotBlank(introduction)) {
            queryWrapper.like("introduction", introduction);
        }
        if (StrUtil.isNotBlank(category)) {
            queryWrapper.eq("category", category);
        }
        if (picSize != null) {
            queryWrapper.eq("picSize", picSize);
        }
        if (picWidth != null) {
            queryWrapper.eq("picWidth", picWidth);
        }
        if (picHeight != null) {
            queryWrapper.eq("picHeight", picHeight);
        }
        if (picScale != null) {
            queryWrapper.eq("picScale", picScale);
        }
        if (StrUtil.isNotBlank(picFormat)) {
            queryWrapper.like("picFormat", picFormat);
        }


        //排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), "ascend".equals(sortOrder), sortField);
        return queryWrapper;
    }

    @Override
    public PictureVO getPictureVo(Picture picture, HttpServletRequest httpServletRequest) {
        PictureVO pictureVO = PictureVO.objToVo(picture);
        Long userId = picture.getUserId();

        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }

    /**
     * 分页获取图片封装
     */
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        // 对象列表 => 封装对象列表
        List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());
        // 1. 关联查询用户信息
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userService.getUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        // 修改数据时，id 不能为空，有参数则校验
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }


}




