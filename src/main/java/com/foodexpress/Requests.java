package com.foodexpress;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
public class Requests {
    public record Registration(@NotBlank @Size(max=80) String name,@NotBlank @Email @Size(max=180) String email,@NotBlank @Size(min=8,max=72) String password) {
    }
    public record CartLine(@NotNull @Positive Long menuItemId,@Min(1) @Max(20) int quantity) {
    }
    public record Checkout(@NotBlank @Size(min=10,max=600) String address,@NotBlank @Pattern(regexp="[0-9]{10}",message="Phone must contain 10 digits") String phone,@Size(max=500) String notes,@NotEmpty @Size(max=30) List<@Valid CartLine> items) {
    }
    public record RestaurantInput(@NotBlank @Size(max=100) String name,@NotBlank @Size(max=100) String cuisine,@NotBlank @Size(max=250) String address,@NotBlank @Size(max=12) String emoji,boolean active) {
    }
    public record MenuInput(@NotNull @Positive Long restaurantId,@NotBlank @Size(max=100) String name,@NotBlank @Size(max=1000) String description,@NotNull @DecimalMin("1.00") @DecimalMax("10000.00") @Digits(integer=5,fraction=2) BigDecimal price,@NotNull @Positive Long categoryId,@NotBlank @Size(max=12) String emoji,boolean vegetarian,boolean available) {
    }
    public record CategoryInput(@NotBlank @Size(max=60) String name) {
    }
    public record UserEnabled(boolean enabled) {
    }
    public record StatusInput(@NotBlank String status) {
    }
}
