package com.project.gulimallorder.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.project.gulimallorder.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private   String app_id = "2016102800775128";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private  String merchant_private_key = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCpX06PaWIjcKI97q6BKU6McHP3D22mRAI/7aFddLEs24GdST5kCaCPut172GV0uL28AYHmHKF8JmS4Vn+aIWtBE0HNflR/LoP6dLbKTd+iSy9c1TNKOjdG4DpfPEpfIQrk1zTVTPc9RWRDGAQC7EJJ0DwRHlq2MdH2A1eH7f7zdS8e7+C87FSokl1nyM3agDDpjAk4HDNdmJ5vwoaeZAV1XkPxhSytnWVc7V8sI6Syhca2lF7nbsWs5jSVaivmihcHarD+l1NnVUxSmeyMIZC1QaFAeb/osU5UtjbBxikisQnRIJ+3bdcyre2f8Hlv8lGkCcS6DkxR4AD9MphWt1pLAgMBAAECggEAUrfYP1QheNt+cS7LjQsmcJJPIH8pdwVJ7/zXTTQ7f4Poxm9KCENnZirGFsD1s86x40r9LOwYtfBuSlT8VsMxhYOpJHopPD/0dvJff2gO60064WTCeuL5siS6fV6Nl+4kSuULyYygRw1HzTjCZMcMDM6lN1sSxF6Kg1LYgetrE2z/A6kGVidRczNCLlKBKj1SaPy90Xf+n/3P9uHkOUnS7vFEM0ALWHbp54gQmOEe7s19OC9iCkgtS9q/Y1+4dUBjEAjmLA1b181AAGGyyI4UjAkytm2OdfSFAfoRuL9niRIDhJ9GOszAxL6t7JgeQm7ZfDHy9jyOynF8/f8nR9ro6QKBgQDym7bvKZ6A3lI6sRkTp89JSHphLvKTlzJ7CIs7Zbf1+I5Xa/oPf1QAY2rLVSuKlWzY826sz18FRjs1eI125/ceWSM1UCOrS+FtHUixkA9oIH5dmqMA2ShQg5DxoIkBpHD0/amsPGvHP/qUcVbyJMZBfR2Ik+offNHprG5f1sh6VQKBgQCyuLLMCcbgQVKj9/yWLQpWBswpQPvflcxQ+L7MobJejVfOF5j2UjycOyLOL/nW0SaOvuU2RIvKs9nIy6/gzx8ph90rMgjlmLRY7bgomDrYRQ3hxrWibEBBdEb8LeQfnHB3T7gZGH5mB3wE5O98orQ//T2NSi1hgjseJGIOUyxiHwKBgFySfDAwElIcAwZwMyBj5N2SsVfJ4Tfd9p4puW71tkc+C5piEi29qSDozoQ9wHqF3928rZt1GU3a6ZbSpDAd/RZwbtXV+XZQYvkOtoFCdg6Gb5FqAw68zfxC55i7Z+vfglaJQCf/eiSyTfw8by/ARoIQqqdFwY04RKTEbe1V+HNtAoGAMHWYNW7pN+E+cStKl8W6qbzKGt197hAGCGaKBZ5KaQzWJBGN7iw6Nvpp45Jqoe6wD8B/EgngBaRihumD1OcUQgSO2amFdAvFyt8aMhekzQNcUj24/kNv4M+Qd8xRsKiQbil1d4iGrU+WNxWEQj0ztq4JxIe6algPg4rF87Bltm0CgYAOOabvgYjQ5bjAc+yx1kP2J8nXyQEYo/3M/XDQfaa5CLh91zukVprJKg2w2mJuNBnLK9O6Yz9iPbSb2R+FgO1UOBSTxWcqjwHroCd144Vgu9Qo03hwm3QE223a7zhkajpnjjIO65BqGyGHtVfaohsg5vThvCALeeQfGfvON7dSQQ==";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private  String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAh0bFO0AiDMMUDpInFagUzVg3vxEY5PNv1pE+0zA9auUCCtOykcnpvVBmVSZpY6uhlNA7dXh3Ms1mEO3JfSqMkZL9ASR4PxBWSVBDO5GveJZg0ueH07CE1ZyfG5j4vtfn7QEnlNO9ail9Yq/Ve25i5si2xKZYLNM35cLoUaH94PZYBf5OoVfdEQNa4GCCaU5zr3yWaT39zf0yUA0QGIr1LPFVtwk67O0GRbCH4tT2Ger+dE4dANze0gCf/HpkyQ4MKebamGYeq0dYJ+vQr3a9/NjYvJxbDE1eXeHHA2kmSgRFSkCirNGL6pCuxOLwL10xs5spuOxvc3KJMHfT4haW1QIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private  String notify_url="http://qinfeng.utools.club/payed/notify";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url="http://member.gulimall.com/memberOrder.html";

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}
