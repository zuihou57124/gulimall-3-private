package com.project.gulimallorder.order.listener;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.google.gson.internal.$Gson$Preconditions;
import com.project.gulimallorder.order.config.AlipayTemplate;
import com.project.gulimallorder.order.service.OrderService;
import com.project.gulimallorder.order.vo.PayAsyncVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Controller
public class OrderPayedListener {

    @Autowired
    OrderService orderService;

    @Autowired
    AlipayTemplate alipayTemplate;

    /**
     * @return success
     * 通知支付宝，服务器已收到支付结果的信息,如果成功，修改订单状态为“已付款”
     */
    @ResponseBody
    @PostMapping("/payed/notify")
    public String handleAliPayed(PayAsyncVo payAsyncVo,HttpServletRequest request) throws UnsupportedEncodingException, AlipayApiException {

        if(payAsyncVo!=null){
            //验证签名，确认是支付宝发送的数据
            //获取支付宝POST过来反馈信息
            Map<String,String> params = new HashMap<String,String>();
            Map<String,String[]> requestParams = request.getParameterMap();
            for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
                String name = (String) iter.next();
                String[] values = (String[]) requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i]
                            : valueStr + values[i] + ",";
                }
                //乱码解决，这段代码在出现乱码时使用
                //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
                params.put(name, valueStr);
            }

            boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayTemplate.getAlipay_public_key(), alipayTemplate.getCharset(), alipayTemplate.getSign_type()); //调用SDK验证签名
            if(signVerified){
                System.out.println("签名验证成功");
                String re = orderService.handleAliPayResult(payAsyncVo);
                if(re.equals("success")){
                    return re;
                }

            }else
            {
                System.out.println("签名验证失败");
            }

        }

        return "error";
    }

}
