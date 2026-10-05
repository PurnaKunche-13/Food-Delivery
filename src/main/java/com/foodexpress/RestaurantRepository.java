package com.foodexpress;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RestaurantRepository extends JpaRepository<Restaurant,Long> {
    java.util.List<Restaurant> findAllByOrderByIdAsc();
}
