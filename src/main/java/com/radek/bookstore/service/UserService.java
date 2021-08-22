package com.radek.bookstore.service;

import com.radek.bookstore.model.User;
import com.radek.bookstore.model.dto.UserDto;
import com.radek.bookstore.model.exception.EmailExistsException;
import com.radek.bookstore.model.exception.UserNotFoundException;
import com.radek.bookstore.model.exception.UsernameExistsException;
import org.springframework.data.domain.Page;

import javax.mail.MessagingException;

public interface UserService {
    boolean existByUserId(String userId);
    User registerUser(UserDto userDto) throws UsernameExistsException, EmailExistsException, UserNotFoundException;
    Page<User> findAllUsers(Integer pageNumber, Integer pageSize);
    User findUserByUsername(String username);
    User findUserByEmail(String email);
    User findUserByUsernameOrEmail(String username) throws UserNotFoundException;
    User addNewUser(UserDto userDto, String role, boolean isNonLocked, boolean isActive) throws UserNotFoundException, UsernameExistsException, EmailExistsException;
    User updateUser(String currentUsername, UserDto userDto, String role, boolean isNonLocked, boolean isActive) throws UserNotFoundException, UsernameExistsException, EmailExistsException;
    void deleteUser(String username) throws UserNotFoundException;
    void resetPassword(String email, String newPassword) throws UserNotFoundException, MessagingException;
    void activateUser(String userId) throws UserNotFoundException;
    Page<User> findUserByKeyword(String keyword, Integer pageNumber, Integer pageSize);
    User findUserById(String id) throws UserNotFoundException;
}
