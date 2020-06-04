package com.project.gulimallorder.order.vo;


import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author root
 */
@Data
public class OrderItemVo {

    private Long skuId;

    private String title;

    private String img;

    private Integer count;

    private Boolean checked;

    private BigDecimal total = new BigDecimal(0);

    private List<String> skuAttr;


}
