package com.project.gulimallware.ware.to.mq;

import lombok.Data;

import java.util.List;

@Data
public class StockLockTo {

    private Long id;

    private StockLockDeatilTo detail;
}
