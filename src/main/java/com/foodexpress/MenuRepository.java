package com.foodexpress;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MenuRepository extends JpaRepository<MenuItem,Long> {
    java.util.List<MenuItem> findByRestaurantIdOrderByIdAsc(Long restaurantId);
}
