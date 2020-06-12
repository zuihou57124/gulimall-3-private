package com.project.gulimallware.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.project.gulimallware.ware.to.mq.StockLockTo;
import com.project.gulimallware.ware.vo.LockStockResult;
import com.project.gulimallware.ware.vo.OrderVo;
import com.project.gulimallware.ware.vo.SkuHasStockVo;
import com.project.gulimallware.ware.vo.WareSkuLockVo;
import io.renren.common.to.SkuHasStockTo;
import io.renren.common.utils.PageUtils;
import com.project.gulimallware.ware.entity.WareSkuEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author qcw
 * @email zuihou57124@gmail.com
 * @date 2020-04-29 17:27:07
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(WareSkuEntity wareSkuEntity);

    List<SkuHasStockTo> getSkuHasStock(List<Long> skuIds);

    Boolean getSkuHasStockById(Long skuId);

    Boolean lockStockForOrder(WareSkuLockVo wareSkuLockVo);

    void unLock(StockLockTo stockLockTo);

    void unLock(OrderVo orderVo);
}

