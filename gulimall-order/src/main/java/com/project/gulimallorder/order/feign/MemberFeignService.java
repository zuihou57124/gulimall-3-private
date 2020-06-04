package com.project.gulimallorder.order.feign;

import com.project.gulimallorder.order.vo.MemberAddressVo;
import com.project.gulimallorder.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

/**
 * @author root
 */
@FeignClient("gulimall-member-9000")
public interface MemberFeignService {

    @RequestMapping("/member/memberreceiveaddress/{memberId}/address")
    List<MemberAddressVo> getAddressList(@PathVariable("memberId") Long memberId);

}
