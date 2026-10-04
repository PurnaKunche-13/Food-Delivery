package com.foodexpress;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CartRepository extends JpaRepository<AppCart,Long>{java.util.Optional<AppCart> findByCustomerId(Long id);}
