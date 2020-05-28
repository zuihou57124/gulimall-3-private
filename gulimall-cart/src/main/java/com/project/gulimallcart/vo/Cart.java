package com.project.gulimallcart.vo;


import java.math.BigDecimal;
import java.util.List;

public class Cart {

    /**
     * 购物车总共有多少个商品
     */
    private Integer countNum;

    /**
     * 商品类型
     */
    private Integer countType;

    /**
     * 购物车里面的商品项
     */
    private List<CartItemVo> items;

    /**
     * 总价
     */
    private BigDecimal total;

    /**
     * 减免
     */
    private BigDecimal reduce = new BigDecimal(0);


    public Integer getCountNum() {
        int count = 0;
        if(items!=null && items.size()>0) {
            for (CartItemVo item : items) {
                count = count + item.getCount();
            }
        }
        return count;
    }

    public void setCountNum(Integer countNum) {
        this.countNum = countNum;
    }

    public Integer getCountType() {
        int count = 0;
        if(items!=null && items.size()>0) {
            for (CartItemVo item : items) {
                count++;
            }
        }
        return count;
    }

    public void setCountType(Integer countType) {
        this.countType = countType;
    }

    public List<CartItemVo> getItems() {
        return items;
    }

    public void setItems(List<CartItemVo> items) {
        this.items = items;
    }

    public BigDecimal getTotal() {
        BigDecimal total = new BigDecimal(0);
        for (CartItemVo item : items) {
            total = total.add(item.getTotal());
        }
        total = total.subtract(this.getReduce());

        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getReduce() {

        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }

}
