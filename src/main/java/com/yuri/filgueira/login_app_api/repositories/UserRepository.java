package com.yuri.filgueira.login_app_api.repositories;

import com.yuri.filgueira.login_app_api.entities.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.email =:email")
    User findByEmail(@Param("email") String email);
}
