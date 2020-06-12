package com.project.gulimallware.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.project.gulimallware.ware.entity.WareOrderTaskDetailEntity;
import com.project.gulimallware.ware.entity.WareOrderTaskEntity;
import com.project.gulimallware.ware.exception.NoStockException;
import com.project.gulimallware.ware.feign.OrderFeignService;
import com.project.gulimallware.ware.feign.SkuInfoFeignService;
import com.project.gulimallware.ware.service.WareOrderTaskDetailService;
import com.project.gulimallware.ware.service.WareOrderTaskService;
import com.project.gulimallware.ware.to.mq.StockLockDeatilTo;
import com.project.gulimallware.ware.to.mq.StockLockTo;
import com.project.gulimallware.ware.vo.*;
import com.rabbitmq.client.Channel;
import io.renren.common.to.SkuHasStockTo;
import io.renren.common.to.SkuTo;
import io.renren.common.utils.R;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;

import com.project.gulimallware.ware.dao.WareSkuDao;
import com.project.gulimallware.ware.entity.WareSkuEntity;
import com.project.gulimallware.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    SkuInfoFeignService skuInfoFeignService;

    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    WareOrderTaskService wareOrderTaskService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    OrderFeignService orderFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        String wareId = (String) params.get("wareId");
        if(!StringUtils.isEmpty(skuId)){
            queryWrapper.and((wrapper->{
                wrapper.eq("sku_id",skuId);
            }));
        }
        if(!StringUtils.isEmpty(wareId)){
            queryWrapper.and((wrapper->{
                wrapper.eq("ware_id",wareId);
            }));
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    /**
     * 商品入库
     */
    @Override
    public void addStock(WareSkuEntity wareSkuEntity) {
        //入库之前查询是否有此商品库存，没有则新建，有则更新
        WareSkuEntity wareSku = this.getOne(
                new QueryWrapper<WareSkuEntity>().eq("sku_id",wareSkuEntity.getSkuId())
                .eq("ware_id",wareSkuEntity.getWareId())
        );
        if(wareSku==null){
            //如果是第一次入库，则要远程获取sku_name,然后再保存
 /*           Map<String, SkuTo> r = skuInfoFeignService.skuInfo(wareSkuEntity.getSkuId());
            if(r.get("skuTo")==null){
                log.error("sku远程服务调用失败");
            }else {
                SkuTo skuTo = (SkuTo) r.get("skuTo");*/
                //wareSkuEntity.setSkuName(skuTo.getSkuName());
                this.save(wareSkuEntity);
           // }
        }
        else {
            wareSku.setStock(wareSku.getStock()+wareSkuEntity.getStock());
            this.updateById(wareSku);
        }


    }

    @Override
    public List<SkuHasStockTo> getSkuHasStock(List<Long> skuIds) {

        List<SkuHasStockTo> skuhasStockVoList = skuIds.stream().map((skuId -> {
            SkuHasStockTo skuHasStockTo = new SkuHasStockTo();
            Long count = this.baseMapper.getStockBySkuId(skuId);
            skuHasStockTo.setSkuId(skuId);
            if(count==null){
                skuHasStockTo.setHasStock(false);
            }else {
                skuHasStockTo.setHasStock(count>0);
            }
            return skuHasStockTo;
        })).collect(Collectors.toList());

        return skuhasStockVoList;
    }

    @Override
    public Boolean getSkuHasStockById(Long skuId) {

        Long count = this.baseMapper.getStockBySkuId(skuId);

        return count!=null && count>0;
    }


    /**
     * @param stockLockTo
     * @param message
     * @param channel
     *
     * 监听队列，判断是否需要解锁库存
     */
    @RabbitListener(queues = "stock.release.stock.queue")
    public void releaseStockHdanler(StockLockTo stockLockTo, Message message, Channel channel){
        //解锁逻辑：
        // 1）、其他业务比如订单业务发生异常，需要解锁，这是需要处理的逻辑
        // 2）、锁库存本身出现异常，系统会自动回滚 ，无需处理
        // 可以通过获得库存工作单的id有无来判断解锁

        StockLockDeatilTo detail = stockLockTo.getDetail();
        WareOrderTaskDetailEntity detailFromDb = wareOrderTaskDetailService.getById(detail.getId());
        if(detailFromDb==null){

        }else {

            //解锁的不同情况：
            // 1）没有对应的订单：解锁
            // 2）有对应的订单：
            //              订单状态：已取消-解锁
            //                       没有取消-不解锁

            Long id = stockLockTo.getId();
            WareOrderTaskEntity taskOrder = wareOrderTaskService.getById(id);
            //远程获取订单的状态
            R r = orderFeignService.getOrderStatus(taskOrder.getOrderSn());
            if(r.getCode()==0){
                OrderVo orderVo = r.getData("data", new TypeReference<OrderVo>(){});
                if(orderVo==null || orderVo.getStatus()==4){
                        //用户取消订单或者订单不存在-解锁
                        unLock(detail);
                }

            }

        }

    }


    /**
     * 解锁库存
     */
    public void unLock(StockLockDeatilTo detail){
        Integer integer = this.baseMapper.uoLock(detail.getSkuId(), detail.getSkuNum(), detail.getWareId());
        
    }


    /**
     * @param wareSkuLockVo
     * @return
     * 为订单锁库存
     *
     * 什么情况下库存需要解锁
     * 1）、用户手动取消订单或者 超过订单有效时间
     * 2）、下单时，库存锁成功，但是其他业务比如订单业务出现异常，导致订单回滚，库存也要随之回滚
     *
     */
    @Transactional
    @Override
    public Boolean lockStockForOrder(WareSkuLockVo wareSkuLockVo) {

        //为每个要锁库存的订单生成记录，记录锁的信息
        WareOrderTaskEntity wareOrderTask = new WareOrderTaskEntity();
        wareOrderTask.setOrderSn(wareSkuLockVo.getOrderSn());
        wareOrderTaskService.save(wareOrderTask);

        List<OrderItemVo> locks = wareSkuLockVo.getLocks();
        //首先查询哪些仓库有此sku
        List<SkuWareStock> skuWareStockList = locks.stream().map((item) -> {
            SkuWareStock skuWareStock = new SkuWareStock();
            skuWareStock.setSkuId(item.getSkuId());
            List<Long> wareIds = this.baseMapper.skuWareHasStock(item.getSkuId());
            skuWareStock.setWareIds(wareIds);
            skuWareStock.setNum(item.getCount());

            return skuWareStock;
        }).collect(Collectors.toList());

        //锁定库存
        for (SkuWareStock skuWareStock : skuWareStockList) {
            boolean skuStock = false;
            Long skuId = skuWareStock.getSkuId();
            List<Long> wareIds = skuWareStock.getWareIds();
            if(wareIds!=null && wareIds.size()>0){
                for (Long wareId : wareIds) {
                    Long count = this.baseMapper.lockStock(skuId, wareId, skuWareStock.getNum().longValue());
                    if(count==1L){
                        //为订单项追踪锁信息
                        WareOrderTaskDetailEntity wareOrderTaskDetail = new WareOrderTaskDetailEntity(null, skuId, null
                                , skuWareStock.getNum(), wareOrderTask.getId(), wareId, 1);
                        wareOrderTaskDetailService.save(wareOrderTaskDetail);
                        StockLockTo stockLockTo = new StockLockTo();

                        //发送消息到队列
                        StockLockDeatilTo stockLockDeatilTo = new StockLockDeatilTo();
                        BeanUtils.copyProperties(wareOrderTaskDetail,stockLockDeatilTo);
                        stockLockTo.setId(wareOrderTask.getId());
                        stockLockTo.setDetail(stockLockDeatilTo);
                        rabbitTemplate.convertAndSend("stock-event-exchange","stock.lock",stockLockTo);

                        skuStock = true;
                        break;
                    }
                }
                if (!skuStock){
                    throw new NoStockException(skuId);
                }

            }else {
                //没有任何仓库有该商品时，抛出异常
                throw new NoStockException(skuId);
            }
        }

        return true;
    }


    /**
     * sku在哪些仓库有货 -- 封装类
     */
    @Data
    static class SkuWareStock{
        private Long skuId;

        private List<Long> wareIds;

        private Integer num;
    }

}