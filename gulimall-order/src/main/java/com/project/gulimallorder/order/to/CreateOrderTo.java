package com.project.gulimallorder.order.to;

import com.project.gulimallorder.order.entity.OrderEntity;
import com.project.gulimallorder.order.entity.OrderItemEntity;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderTo {

    private OrderEntity order;

    private List<OrderItemEntity> orderItemList;

    private BigDecimal payPrice;

    private BigDecimal fare;


}
