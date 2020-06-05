package com.project.gulimallware.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.project.gulimallware.ware.feign.MemberFeignService;
import com.project.gulimallware.ware.vo.MemberAddressVo;
import io.renren.common.utils.R;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;

import com.project.gulimallware.ware.dao.WareInfoDao;
import com.project.gulimallware.ware.entity.WareInfoEntity;
import com.project.gulimallware.ware.service.WareInfoService;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<WareInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            queryWrapper.and((wrapper->{
                wrapper.eq("id",key)
                        .or()
                        .like("name",key)
                        .or()
                        .like("address",key);
            }));
        }

        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public BigDecimal getFare(Long addrId) {

        R r = memberFeignService.info(addrId);
        MemberAddressVo address = r.getData("memberReceiveAddress", new TypeReference<MemberAddressVo>() {});
        if(address!=null){
            String phone = address.getPhone();
            String substring = phone.substring(phone.length() - 2);
            return new BigDecimal(substring);
        }
        return null;
    }

}