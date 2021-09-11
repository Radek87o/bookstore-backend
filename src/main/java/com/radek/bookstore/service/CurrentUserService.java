package com.radek.bookstore.service;

import com.radek.bookstore.model.User;
import com.radek.bookstore.model.exception.BookStoreServiceException;
import com.radek.bookstore.repository.UserRepository;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            String username;
            if (principal instanceof UserDetails) {
                username = ((UserDetails)principal).getUsername();
            } else {
                username = principal.toString();
            }
            return userRepository.findUserByCredentials(username);
        } catch (NonTransientDataAccessException exc) {
            throw new BookStoreServiceException("Error occurred by attempt to get current user");
        }
    }
}
