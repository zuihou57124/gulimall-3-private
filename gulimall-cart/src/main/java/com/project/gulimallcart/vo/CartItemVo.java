package com.project.gulimallcart.vo;

import java.math.BigDecimal;
import java.util.List;

public class CartItemVo {

    private Long skuId;

    private String title;

    private String img;

    private Integer count;

    private Boolean checked;

    private BigDecimal total;

    private List<String> skuAttr;

    private BigDecimal price;


    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    public BigDecimal getTotal() {

        return this.total.multiply(new BigDecimal(count));
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<String> getSkuAttr() {
        return skuAttr;
    }

    public void setSkuAttr(List<String> skuAttr) {
        this.skuAttr = skuAttr;
    }
}
