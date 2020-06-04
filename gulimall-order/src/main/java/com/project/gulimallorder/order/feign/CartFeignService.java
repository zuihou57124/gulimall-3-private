package com.project.gulimallorder.order.feign;

import com.project.gulimallorder.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

/**
 * @author root
 */
@FeignClient("gulimall-cart-15000")
public interface CartFeignService {

    @RequestMapping("/currentUserCartItems")
    List<OrderItemVo> getCurrentUserCartItems();

}
