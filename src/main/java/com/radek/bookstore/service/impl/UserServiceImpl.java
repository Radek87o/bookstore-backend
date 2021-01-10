package com.radek.bookstore.service.impl;

import com.radek.bookstore.repository.UserRepository;
import com.radek.bookstore.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean existByUserId(String userId) {
        return userRepository.existsById(userId);
    }
}
