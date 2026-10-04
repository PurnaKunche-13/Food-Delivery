package com.foodexpress;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
@Entity
@Table(name="food_orders")
public class FoodOrder {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Version @JsonIgnore public Long version;
 @JsonIgnore @Column(nullable=false) public Long customerId;
 public String customerName;
 public Long restaurantId;
 public String restaurantName;
 @Column(length=600) public String address;
 public String phone;
 @Column(length=500) public String notes;
 public String status="PLACED";
 public String paymentMethod="CASH_ON_DELIVERY";
 public String paymentStatus="UNPAID";
 public java.time.Instant createdAt=java.time.Instant.now();
 @Column(precision=12,scale=2) public java.math.BigDecimal subtotal;
 @Column(precision=12,scale=2) public java.math.BigDecimal deliveryFee;
 @Column(precision=12,scale=2) public java.math.BigDecimal total;
 @ElementCollection(fetch=FetchType.EAGER) @CollectionTable(name="order_lines",joinColumns=@JoinColumn(name="order_id"))
 @OrderColumn(name="line_position") public java.util.List<OrderLine> items=new java.util.ArrayList<>();
}
