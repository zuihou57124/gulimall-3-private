package com.project.gulimallware.ware.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FareVo {

    BigDecimal fare;

    MemberAddressVo address;

}
