package com.project.gulimallcart.vo;

import lombok.Data;

@Data
public class UserInfoTo {

    private Long userId;

    private String userKey;


    /**
     * 提示浏览器是否需要创建新的cookie，默认需要创建
     */
    private Boolean isTemp = true;

}
