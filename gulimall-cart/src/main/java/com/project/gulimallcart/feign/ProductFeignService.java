package com.project.gulimallcart.feign;

import io.renren.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-product-11000")
public interface ProductFeignService {

    @RequestMapping("/product/skuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);

}
