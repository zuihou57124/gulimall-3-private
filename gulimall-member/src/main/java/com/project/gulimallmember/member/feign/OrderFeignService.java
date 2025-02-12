package com.project.gulimallmember.member.feign;

import io.renren.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient("gulimall-order-10000")
public interface OrderFeignService {

    @RequestMapping("/order/order/list")
    R list(@RequestParam Map<String, Object> params);

}
