package com.project.gulimallorder.order.to.mq;

import lombok.Data;

@Data
public class StockLockTo {

    private Long id;

    private StockLockDeatilTo detail;
}
