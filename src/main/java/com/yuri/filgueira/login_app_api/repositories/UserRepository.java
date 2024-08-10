package com.yuri.filgueira.login_app_api.repositories;

import com.yuri.filgueira.login_app_api.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
