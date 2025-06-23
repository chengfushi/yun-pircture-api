package com.chengfu.yunpictureapi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chengfu.yunpictureapi.model.entity.Picture;
import com.chengfu.yunpictureapi.service.PictureService;
import com.chengfu.yunpictureapi.mapper.PictureMapper;
import org.springframework.stereotype.Service;

/**
* @author Lenovo
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-06-23 11:35:45
*/
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{

}




