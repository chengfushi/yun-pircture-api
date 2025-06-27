package com.chengfu.yunpictureapi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chengfu.yunpictureapi.model.dto.picture.PictureQueryRequest;
import com.chengfu.yunpictureapi.model.dto.picture.PictureReviewRequest;
import com.chengfu.yunpictureapi.model.dto.picture.PictureUploadRequest;
import com.chengfu.yunpictureapi.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chengfu.yunpictureapi.model.entity.User;
import com.chengfu.yunpictureapi.model.vo.picture.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
* @author Lenovo
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-06-23 11:35:45
*/
public interface PictureService extends IService<Picture> {


    /**
     * 上传图片
     *
     * @param multipartFile
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(MultipartFile multipartFile,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);

    /*
     * 将查询条件封装成QueryWrapper
     * @param pictureQueryRequest 图片查询请求
     * @return QueryWrapper 图片查询条件
     * */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /*
     * 获取单个图片封装类
     * @param picture 图片实体类
     * @return PictureVO 图片封装类
     * */
    PictureVO getPictureVo(Picture picture, HttpServletRequest httpServletRequest);

    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    void validPicture(Picture picture);

    /*
     * */
    boolean doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    void fillReviewParams(Picture picture, User loginUser);
}
