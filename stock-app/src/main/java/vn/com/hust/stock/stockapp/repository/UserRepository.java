package vn.com.hust.stock.stockapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.com.hust.stock.stockmodel.user.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByUserName(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
}
