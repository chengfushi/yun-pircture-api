package com.chengfu.yunpictureapi.api.imagesearch;

import com.chengfu.yunpictureapi.api.imagesearch.model.ImageSearchResult;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @BelongsProject: yun-picture-api
 * @BelongsPackage: com.chengfu.yunpictureapi.api.imagesearch
 * @Author: Chengfu Shi
 * @CreateTime: 2025-07-02 17:16
 * @Description: TODO
 * @Version: 1.0
 **/
@Slf4j
public class ImageSearchApiFacade {

    /**
     * 搜索图片
     *
     * @param imageUrl
     * @return
     */
    public static List<ImageSearchResult> searchImage(String imageUrl) {
        String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(imageUrl);
        String imageFirstUrl = GetImageFirstUrlApi.getImageFirstUrl(imagePageUrl);
        List<ImageSearchResult> imageList = GetImageListApi.getImageList(imageFirstUrl);
        return imageList;
    }

    public static void main(String[] args) {
        // 测试以图搜图功能
        String imageUrl = "https://www.codefather.cn/logo.png";
        List<ImageSearchResult> resultList = searchImage(imageUrl);
        System.out.println("结果列表" + resultList);
    }
}


