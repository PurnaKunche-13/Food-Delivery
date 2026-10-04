package com.foodexpress;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
@Entity
@Table(name="app_users",uniqueConstraints=@UniqueConstraint(columnNames="email"))
public class AppUser {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public String name;
 @Column(nullable=false) public String email;
 @JsonIgnore @Column(nullable=false) public String password;
 public String role="CUSTOMER";
 public boolean enabled=true;
 @JsonIgnore @Column(unique=true) public String googleSubject;
}
