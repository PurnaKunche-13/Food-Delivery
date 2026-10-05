package com.foodexpress;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
@Embeddable
public class OrderLine {
    public Long menuItemId;
    public String name;
    public int quantity;
    @Column(precision=12,scale=2) public java.math.BigDecimal unitPrice;
    @Column(precision=12,scale=2) public java.math.BigDecimal lineTotal;
    public OrderLine() {
    }
    public OrderLine(MenuItem m,int q) {
        menuItemId=m.id;
        name=m.name;
        quantity=q;
        unitPrice=m.price;
        lineTotal=m.price.multiply(java.math.BigDecimal.valueOf(q));
    }
}
