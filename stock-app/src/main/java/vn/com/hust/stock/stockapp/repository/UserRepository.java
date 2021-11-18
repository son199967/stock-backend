package vn.com.hust.stock.stockapp.repository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.hust.stock.stockmodel.user.User;


public interface UserRepository extends JpaRepository<User, Integer> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    User findByUsername(String username);

    @Transactional
    void deleteByUsername(String username);

}
