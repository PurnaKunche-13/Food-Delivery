package com.foodexpress;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UserRepository extends JpaRepository<AppUser,Long> {
    java.util.Optional<AppUser> findByEmail(String email);
}
