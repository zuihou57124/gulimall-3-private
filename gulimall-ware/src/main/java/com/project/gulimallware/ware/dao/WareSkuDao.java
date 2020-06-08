package com.project.gulimallware.ware.dao;

import com.project.gulimallware.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author qcw
 * @email zuihou57124@gmail.com
 * @date 2020-04-29 17:27:07
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    Long getStockBySkuId(Long skuId);

    List<Long> skuWareHasStock(Long skuId);

    Long lockStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("num") Long num);

}
