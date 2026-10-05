package com.foodexpress;
import jakarta.persistence.*;
@Entity @Table(name="categories",uniqueConstraints=@UniqueConstraint(columnNames="name"))
public class Category {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false) public String name;
}
