package com.project.gulimallorder.order.web;


import com.project.gulimallorder.order.service.OrderService;
import com.project.gulimallorder.order.vo.OrderConfirmVo;
import com.project.gulimallorder.order.vo.OrderSubmitVo;
import com.project.gulimallorder.order.vo.SubmitOrderRespVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class IndexController {

    @Autowired
    OrderService orderService;

    @RequestMapping("/{page}.html")
    public String getPage(@PathVariable("page") String page){
        return page;
    }

    @RequestMapping("/toTrade")
    public String toTrade(Model model, HttpServletRequest request){
        request.getSession();

        OrderConfirmVo orderConfirmVo =  orderService.confirmOrder();
        model.addAttribute("orderConfirm",orderConfirmVo);
        return "confirm";
    }

    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo orderSubmitVo, Model model, RedirectAttributes redirectAttributes){

        //初始值为0，出现异常代表失败，不为0
        SubmitOrderRespVo submitOrderRespVo = new SubmitOrderRespVo();
        submitOrderRespVo.setCode(0);
        submitOrderRespVo = orderService.submitOrder(orderSubmitVo);

        if (submitOrderRespVo.getCode()==0){
            //成功后跳到支付页
            model.addAttribute("resp",submitOrderRespVo);
            return "pay";
        }
        String msg = "下单失败";
        switch (submitOrderRespVo.getCode()){
            case 1:
                msg = msg+"订单信息过期，请重新下单";
                break;
            case 2:
                msg = msg+"价格发生变化，请重新确认订单";
                break;
            case 3:
                msg = msg+"库存不足";
                break;
        }
        redirectAttributes.addFlashAttribute("error",msg);
        //失败后跳到确认页面，重新确认订单信息
        return "redirect:http://order.gulimall.com/toTrade";
    }


}
