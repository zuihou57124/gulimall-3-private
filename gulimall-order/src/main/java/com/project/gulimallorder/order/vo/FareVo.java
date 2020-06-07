package com.project.gulimallorder.order.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FareVo {

    BigDecimal fare;

    MemberAddressVo address;

}
