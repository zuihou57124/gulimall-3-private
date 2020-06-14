package com.project.gulimallmember.member.web;

import com.alibaba.fastjson.JSONObject;
import com.project.gulimallmember.member.feign.OrderFeignService;
import io.renren.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    OrderFeignService orderFeignService;

    @RequestMapping("/memberOrder.html")
    public String orderListPage(@RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                                Model model){

        HashMap<String, Object> params = new HashMap<>();
        params.put("page",pageNum);
        R r = orderFeignService.list(params);
        model.addAttribute("orders", r);

        return "orderList";
    }

}
