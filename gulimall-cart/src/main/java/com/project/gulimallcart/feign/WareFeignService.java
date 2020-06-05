package com.project.gulimallcart.feign;

import io.renren.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author qcw
 * 仓库服务远程接口
 */
@FeignClient("gulimall-ware-12000")
public interface WareFeignService {

    @RequestMapping("/ware/waresku/{skuId}/hasstock")
    R hasStock(@PathVariable("skuId") Long skuId);

}
