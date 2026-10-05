package com.foodexpress;
import jakarta.persistence.*;
@Entity @Table(name="customer_carts",uniqueConstraints=@UniqueConstraint(columnNames="customerId"))
public class AppCart {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Version public Long version;
    @Column(nullable=false) public Long customerId;
    @ElementCollection(fetch=FetchType.EAGER) @CollectionTable(name="cart_lines",joinColumns=@JoinColumn(name="cart_id"))
    public java.util.List<CartEntry> items=new java.util.ArrayList<>();
}
