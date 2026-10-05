package com.foodexpress;
import org.springframework.data.jpa.repository.JpaRepository;
public interface OrderRepository extends JpaRepository<FoodOrder,Long> {
    java.util.List<FoodOrder> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    java.util.List<FoodOrder> findAllByOrderByCreatedAtDesc();
}
