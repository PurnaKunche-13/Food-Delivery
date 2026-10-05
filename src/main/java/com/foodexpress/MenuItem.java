package com.foodexpress;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
@Entity
public class MenuItem {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false) public Long restaurantId;
    @Column(nullable=false) public String name;
    @Column(length=1000) public String description;
    @Column(nullable=false,precision=12,scale=2) public java.math.BigDecimal price;
    public String category;
    public Long categoryId;
    public String emoji;
    public boolean vegetarian;
    public boolean available=true;
}
