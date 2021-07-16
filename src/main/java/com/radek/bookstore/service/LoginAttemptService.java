package com.radek.bookstore.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

import static java.util.concurrent.TimeUnit.MINUTES;

@Service
public class LoginAttemptService {

    private static final Logger logger = LoggerFactory.getLogger(LoginAttemptService.class);

    public static final int MAXIMUM_NUMBER_OF_ATTEMPTS = 5;
    public static final int ATTEMPT_INCREMENT = 1;
    private final LoadingCache<String, Integer> loginAttemptCache;

    public LoginAttemptService() {
        super();
        this.loginAttemptCache = CacheBuilder.newBuilder()
                                    .expireAfterWrite(15, MINUTES).maximumSize(100)
                                    .build(new CacheLoader<String, Integer>() {
                                        @Override
                                        public Integer load(String key) throws Exception {
                                            return 0;
                                        }
        });
    }

    public void evictUserFromLoginAttemptCache(String username) {
        loginAttemptCache.invalidate(username);
    }

    public void addUserToLoginAttemptCache(String username) {
        int attempts = 0;
        try {
            attempts=ATTEMPT_INCREMENT+loginAttemptCache.get(username);
            loginAttemptCache.put(username, attempts);
        } catch (ExecutionException exc) {
            logger.info("Execution exception by attempt to add user to login attempt cache after incorrect login attempt");
        }
    }

    public boolean exceededMaxAttempts(String username)  {
        try {
            return loginAttemptCache.get(username)>MAXIMUM_NUMBER_OF_ATTEMPTS;
        } catch (ExecutionException exc) {
            logger.info("Execution exception by attempt to check whether user exceeded number of allowed login attempts");
        }
        return false;
    }
}
