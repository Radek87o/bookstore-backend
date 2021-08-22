package com.radek.bookstore.repository;

import com.radek.bookstore.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    @Query(value = "SELECT u FROM User u WHERE u.username=:username OR u.email=:username")
    Optional<User> findByUsernameOrEmail(@Param("username") String username);

    @Query(value = "SELECT u FROM User u WHERE u.username=:username OR u.email=:username")
    User findUserByCredentials(@Param("username") String username);

    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    User findUserByUsername(String username);
    User findUserByEmailIgnoreCase(String email);
    User findUserByUserId(String userId);
    User findUserById(String id);

    @Transactional
    @Query(nativeQuery = true, value = "SELECT * FROM user u " +
            "WHERE u.first_name LIKE CONCAT ('%',:keyword,'%') OR u.last_name LIKE CONCAT ('%',:keyword,'%') " +
            "OR u.email LIKE CONCAT ('%',:keyword,'%') OR u.username LIKE CONCAT ('%',:keyword,'%') " +
            "order by u.created_date DESC")
    Page<User> findUserByKeyword(String keyword, Pageable pageable);
}
