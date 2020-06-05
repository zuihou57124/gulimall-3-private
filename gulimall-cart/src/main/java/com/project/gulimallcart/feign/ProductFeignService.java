package com.project.gulimallcart.feign;

import io.renren.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;

@FeignClient("gulimall-product-11000")
public interface ProductFeignService {

    @RequestMapping("/product/skuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);

    @RequestMapping("/product/skusaleattrvalue/saleAttrs/{skuId}")
    R list(@PathVariable("skuId") Long skuId);

    @RequestMapping("/product/skuinfo/{skuId}/price")
    BigDecimal getPrice(@PathVariable("skuId") Long skuId);

    @RequestMapping("/product/spuinfo/{spuId}/weight")
    R spuWeight(@PathVariable("spuId") Long spuId);

}
