package com.project.gulimallorder.order.feign;

import io.renren.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("gulimall-ware-12000")
public interface WareFeignService {

    @RequestMapping("/ware/wareinfo/fare")
    R getFare(@RequestParam("addrId") Long addrId);

}
