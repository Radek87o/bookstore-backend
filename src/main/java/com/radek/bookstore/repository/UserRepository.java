package com.radek.bookstore.repository;

import com.radek.bookstore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    @Query(value = "SELECT u FROM User u WHERE u.username=:username OR u.email=:username")
    Optional<User> findByUsernameOrEmail(@Param("username") String username);

    @Query(value = "SELECT u FROM User u WHERE u.username=:username OR u.email=:email")
    Optional<User> findByCredentials(@Param("username") String username, @Param("email") String email);

    @Query(value = "SELECT u FROM User u WHERE u.username=:username OR u.email=:username")
    User findUserByCredentials(@Param("username") String username);

    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    User findUserByUsername(String username);
    User findUserByEmailIgnoreCase(String email);
    User findUserByUserId(String userId);
}
