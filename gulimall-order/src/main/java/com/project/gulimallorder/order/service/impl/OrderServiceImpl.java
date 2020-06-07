package com.project.gulimallorder.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.project.gulimallorder.order.constant.OrderConst;
import com.project.gulimallorder.order.constant.OrderEnum;
import com.project.gulimallorder.order.entity.OrderItemEntity;
import com.project.gulimallorder.order.feign.ProductFeignService;
import com.project.gulimallorder.order.feign.WareFeignService;
import com.project.gulimallorder.order.interceptor.OrderInterceptor;
import com.project.gulimallorder.order.feign.CartFeignService;
import com.project.gulimallorder.order.feign.MemberFeignService;
import com.project.gulimallorder.order.to.CreateOrderTo;
import com.project.gulimallorder.order.vo.*;
import io.renren.common.to.SpuBoundTo;
import io.renren.common.utils.R;
import io.renren.common.vo.MemberRespVo;
import javafx.scene.layout.BorderImage;
import org.omg.CORBA.ORB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;

import com.project.gulimallorder.order.dao.OrderDao;
import com.project.gulimallorder.order.entity.OrderEntity;
import com.project.gulimallorder.order.service.OrderService;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> threadLocal = new ThreadLocal<>();

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 获取用户确认订单的信息
     */
    @Override
    public OrderConfirmVo confirmOrder() {

        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        MemberRespVo memberRespVo = OrderInterceptor.threadLocal.get();

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> addressTask = CompletableFuture.runAsync(() -> {
            //远程查询用户地址
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> addressList = memberFeignService.getAddressList(memberRespVo.getId());
            orderConfirmVo.setMemberAddressVoList(addressList);
        },executor);

        CompletableFuture<Void> currentUserCartItemsTask = CompletableFuture.runAsync(() -> {
            //远程查询购物车被选中的选项
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
            orderConfirmVo.setOrderItemVoList(currentUserCartItems);
        },executor);

        //查询用户积分信息
        orderConfirmVo.setIntegration(memberRespVo.getIntegration());

        //防重令牌
        String uuid = UUID.randomUUID().toString().replace("-", "");
        orderConfirmVo.setOrderToken(uuid);
        redisTemplate.opsForValue().set(OrderConst.USER_ORDER_TOKEN_PRIFIX+memberRespVo.getId(),uuid,20, TimeUnit.MINUTES);

        try {
            CompletableFuture.allOf(addressTask,currentUserCartItemsTask).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return orderConfirmVo;
    }

    @Override
    public SubmitOrderRespVo submitOrder(OrderSubmitVo orderSubmitVo) {

        threadLocal.set(orderSubmitVo);
        SubmitOrderRespVo resp = new SubmitOrderRespVo();
        //需要实现：下单，验证令牌，验证价格，锁库存
        String script = "if redis.call('get',KEYS[1])==ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        MemberRespVo memberRespVo = OrderInterceptor.threadLocal.get();
        //redis存储的token
        String redisToken = redisTemplate.opsForValue().get(OrderConst.USER_ORDER_TOKEN_PRIFIX + memberRespVo.getId());
        //页面传过来的token
        String orderToken = orderSubmitVo.getOrderToken();
        //令牌的对比和删除必须是原子性的
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(OrderConst.USER_ORDER_TOKEN_PRIFIX + memberRespVo.getId()), orderToken);
        if(result!=null && result==1L){
            //通过令牌验证
            CreateOrderTo orderTo = createOrder();
            if(Math.abs(orderTo.getOrder().getPayAmount().subtract(orderSubmitVo.getPayPrice()).doubleValue())<0.01){
                //验证价格成功,保存订单到数据库
                saveOrder(orderTo);

            }
            resp.setCode(0);
            return resp;
        }

        resp.setCode(500);
        /*if(orderToken!=null && orderToken.equals(redisToken)){
            //令牌通过验证，首先删除服务端的令牌
            redisTemplate.delete(OrderConst.USER_ORDER_TOKEN_PRIFIX + memberRespVo.getId());
        }*/

        return resp;
    }

    /**
     * @param orderTo
     * 保存订单
     */
    private void saveOrder(CreateOrderTo orderTo) {

        OrderEntity order = orderTo.getOrder();
        List<OrderItemEntity> orderItemList = orderTo.getOrderItemList();


    }

    private CreateOrderTo createOrder(){

        CreateOrderTo orderTo = new CreateOrderTo();
        //构建订单
        OrderEntity order = buildOrder();

        //构建所有订单项,订单与订单项的关系是 一对多
        List<OrderItemEntity> orderItems = buildOrderItems(order.getOrderSn());

        //根据获得的订单项验证价格
        validatePrice(order,orderItems);

        orderTo.setOrder(order);
        orderTo.setOrderItemList(orderItems);

        return orderTo;

    }

    /**
     * @param order 订单
     * @param orderItems 订单项
     * 验证已选中的购物项的实际总价
     */
    private void validatePrice(OrderEntity order, List<OrderItemEntity> orderItems) {

        BigDecimal total = new BigDecimal(0);
        BigDecimal couponAmount = new BigDecimal(0);
        BigDecimal integrationAmount = new BigDecimal(0);
        BigDecimal promotionAmount = new BigDecimal(0);

        int giftIntegration = 0;
        int giftGrowth = 0;

        if(orderItems!=null){
            for (OrderItemEntity orderItem : orderItems) {
                total = total.add(orderItem.getRealAmount());
                couponAmount = couponAmount.add(orderItem.getCouponAmount());
                integrationAmount = integrationAmount.add(orderItem.getIntegrationAmount());
                promotionAmount = promotionAmount.add(orderItem.getPromotionAmount());

                giftGrowth = giftGrowth+orderItem.getGiftGrowth();
                giftIntegration = giftIntegration+orderItem.getGiftIntegration();

            }
        }

        //设置订单的优惠等总额
        order.setPromotionAmount(promotionAmount);
        order.setIntegrationAmount(integrationAmount);
        order.setCouponAmount(couponAmount);
        //设置订单应付总额
        order.setTotalAmount(total);
        order.setPayAmount(total.add(order.getFreightAmount()));
        //订单赠送的积分等信息
        order.setGrowth(giftGrowth);
        order.setIntegration(giftIntegration);
        //订单是否删除
        order.setDeleteStatus(0);

    }

    /**
     * 构建订单
     */
    private OrderEntity buildOrder() {
        //根据时间生成订单号
        String orderSn = IdWorker.getTimeId();
        OrderEntity order = new OrderEntity();
        order.setOrderSn(orderSn);
        MemberRespVo member = OrderInterceptor.threadLocal.get();
        order.setMemberId(member.getId());
        order.setMemberUsername(member.getUsername());
        order.setModifyTime(new Date());
        //远程获取运费和地址信息
        R r = wareFeignService.getFare(threadLocal.get().getAddrId());
        FareVo fareVo = r.getData("data", new TypeReference<FareVo>(){});
        //设置收货信息
        order.setFreightAmount(fareVo.getFare());
        order.setReceiverCity(fareVo.getAddress().getCity());
        order.setReceiverName(fareVo.getAddress().getName());
        order.setReceiverProvince(fareVo.getAddress().getProvince());
        order.setReceiverDetailAddress(fareVo.getAddress().getDetailAddress());
        order.setReceiverPhone(fareVo.getAddress().getPhone());

        //新建订单的状态
        order.setStatus(OrderEnum.CREATE_NEW.getCode());

        return order;
    }


    /**
     * 构建所有的订单项
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {

        List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        List<OrderItemEntity> orderitems = null;
        if(currentUserCartItems!=null){
            orderitems = currentUserCartItems.stream().map((cartItem) -> {
                OrderItemEntity orderItem = buildOrderItem(cartItem);
                orderItem.setOrderSn(orderSn);

                return orderItem;

            }).collect(Collectors.toList());
        }
        return orderitems;
    }

    /**
     * 构建订单项
     */
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {

        OrderItemEntity orderItemEntity = new OrderItemEntity();
        //商品基本信息(spu信息需要远程查询)
        R r = productFeignService.spuinfoById(cartItem.getSpuId());
        if(r.getCode()==0){
            SpuInfoVo spuInfo = r.getData("spuInfo", new TypeReference<SpuInfoVo>() {});
            orderItemEntity.setCategoryId(spuInfo.getCatalogId());
            orderItemEntity.setSpuName(spuInfo.getSpuName());
            //TODO 品牌名暂时直接使用品牌id代替
            orderItemEntity.setSpuBrand(spuInfo.getBrandId().toString());
        }

        orderItemEntity.setSkuId(cartItem.getSkuId());
        orderItemEntity.setSpuId(cartItem.getSpuId());
        orderItemEntity.setSkuName(cartItem.getTitle());
        orderItemEntity.setSkuPrice(cartItem.getPrice());
        orderItemEntity.setSkuPic(cartItem.getImg());
        orderItemEntity.setSkuAttrsVals(StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(),","));
        orderItemEntity.setSkuQuantity(cartItem.getCount());
        //优惠信息---

        //积分信息
        orderItemEntity.setGiftGrowth(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount())).intValue());
        orderItemEntity.setGiftIntegration(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount())).intValue());

        //价格优惠等信息
        orderItemEntity.setIntegrationAmount(new BigDecimal(0));
        orderItemEntity.setPromotionAmount(new BigDecimal(0));
        orderItemEntity.setCouponAmount(new BigDecimal(0));
        BigDecimal orgin = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity()));
        //减去各种优惠信息后的真实总额
        BigDecimal real = orgin.subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(real);


        return orderItemEntity;

    }


}