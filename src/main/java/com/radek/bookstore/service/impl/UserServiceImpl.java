package com.radek.bookstore.service.impl;

import com.radek.bookstore.model.*;
import com.radek.bookstore.model.dto.AddressDto;
import com.radek.bookstore.model.dto.UserDto;
import com.radek.bookstore.model.exception.*;
import com.radek.bookstore.repository.AddressRepository;
import com.radek.bookstore.repository.UserRepository;
import com.radek.bookstore.service.EmailService;
import com.radek.bookstore.service.LoginAttemptService;
import com.radek.bookstore.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.radek.bookstore.model.Role.ROLE_USER;
import static com.radek.bookstore.utils.PasswordGenerator.generatePassword;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.apache.commons.lang3.StringUtils.*;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.by;

@Service
@Transactional
@Qualifier("userDetailsService")
public class UserServiceImpl implements UserService, UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    public static final String EMAIL_ALREADY_HAS_ACCOUNT = "Email already has account: ";
    public static final String USERNAME_ALREADY_TAKEN = "Username already taken: ";

    private final String profileImageSource;
    private final String appBaseLink;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final BCryptPasswordEncoder encoder;
    private final LoginAttemptService loginAttemptService;
    private final EmailService emailService;

    public UserServiceImpl(@Value("${bookstore.profileImageSource}") String profileImageSource,
                           @Value("${bookstore.appBaseLink}") String appBaseLink,
                           UserRepository userRepository,
                           AddressRepository addressRepository,
                           BCryptPasswordEncoder encoder,
                           LoginAttemptService loginAttemptService,
                           EmailService emailService) {
        this.profileImageSource = profileImageSource;
        this.appBaseLink=appBaseLink;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.encoder = encoder;
        this.loginAttemptService = loginAttemptService;
        this.emailService = emailService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(username)
                .orElseThrow(
                        () -> new UsernameNotFoundException(
                                String.format("Username with credentials %s not found", username)));
        validateLoginAttempt(user);
        user.setLastLoginDate(LocalDateTime.now());
        userRepository.save(user);
        log.info("Returning user found by username: {}", username);
        return new UserPrincipal(user);
    }

    private void validateLoginAttempt(User user) {
        String username = user.getUsername()==null ? user.getEmail() : user.getUsername();
        if(user.isNotLocked()) {
            if(loginAttemptService.exceededMaxAttempts(username)) {
                user.setNotLocked(false);
            } else {
                user.setNotLocked(true);
            }
        } else {
            loginAttemptService.evictUserFromLoginAttemptCache(username);
        }
    }

    @Override
    public boolean existByUserId(String userId) {
        try {
            return userRepository.existsById(userId);
        } catch (NonTransientDataAccessException exc) {
            String message = "An error by checking if user exists by id";
            log.error(message, exc);
            throw new BookStoreServiceException(message, exc);
        }
    }

    @Override
    public User registerUser(UserDto userDto) throws UsernameExistsException, EmailExistsException, UserNotFoundException {
        try {
            String newUsername = isBlank(userDto.getUsername()) ? userDto.getEmail() : userDto.getUsername();
            validateNewUsernameAndEmail(EMPTY, newUsername, userDto.getEmail());
            if(isBlank(userDto.getPassword())) {
                String message = String.format("An error occurred during attempt to register user with email: %s, due to empty password", userDto.getEmail());
                log.info(message);
                throw new PasswordNotExistsException(message);
            }
            User user = populateUserToSave(userDto, false, true, ROLE_USER);
            User savedUser = userRepository.save(user);
            emailService.sendActivationAccountMessage(savedUser.getFirstName(), generateActivationLink(user.getUserId()), user.getEmail());
            log.info("New user with email {} successfully registered", savedUser.getEmail());
            return savedUser;
        } catch(NonTransientDataAccessException | MessagingException | PasswordNotExistsException exc) {
            String message = String.format("An error occurred during attempt to register user with email: %s", userDto.getEmail());
            log.error(message, exc);
            throw new BookStoreServiceException(message, exc);
        }
    }

    @Override
    public Page<User> findAllUsers(Integer pageNumber, Integer pageSize) {
        try {
            return userRepository.findAll(PageRequest.of(pageNumber, pageSize, by(ASC, "lastName")));
        } catch (NonTransientDataAccessException exc) {
            String message = "An error occurred during attempt to extract user's page";
            log.error(message, exc);
            throw new BookStoreServiceException(message, exc);
        }
    }

    @Override
    public User findUserByUsername(String username) {
        try {
            return userRepository.findUserByUsername(username);
        } catch(NonTransientDataAccessException exc) {
            String message = String.format("An error occurred during attempt to extracting user by username: %s", username);
            log.error(message, exc);
            throw new BookStoreServiceException(message, exc);
        }
    }

    @Override
    public User findUserByEmail(String email) {
        try {
            return userRepository.findUserByEmailIgnoreCase(email);
        } catch (NonTransientDataAccessException exc) {
            String message = String.format("An error occurred during attempt to extracting user by email: %s", email);
            log.error(message, exc);
            throw new BookStoreServiceException(message, exc);
        }
    }

    @Override
    public User findUserByUsernameOrEmail(String username) throws UserNotFoundException {
        try {
            User userByCredentials = userRepository.findUserByCredentials(username);
            if(isNull(userByCredentials)) {
                throw new UserNotFoundException(String.format("Cannot find user with username: %s", username));
            }
            return userByCredentials;
        } catch (NonTransientDataAccessException exc) {
            String message = String.format("An error occurred during attempt to extract user by username or email: %s", username);
            log.error(message, exc);
            throw new BookStoreServiceException(message, exc);
        }
    }

    @Override
    public User findUserById(String id) throws UserNotFoundException {
        try {
            Optional<User> userById = userRepository.findById(id);
            if(userById.isEmpty()) {
                throw new UserNotFoundException(String.format("Cannot find user with id: %s", id));
            }
            return userById.get();
        } catch (NonTransientDataAccessException exc) {
            String message = String.format("An error occurred during attempt to extract user by id: %s", id);
            log.error(message, exc);
            throw new BookStoreServiceException(message, exc);
        }
    }

    @Override
    public User addNewUser(UserDto userDto, String role, boolean isNonLocked, boolean isActive) throws UserNotFoundException, UsernameExistsException, EmailExistsException {
        try {
            String newUsername = isBlank(userDto.getUsername()) ? userDto.getEmail() : userDto.getUsername();
            validateNewUsernameAndEmail(EMPTY, newUsername, userDto.getEmail());
            Role newUserRole = getRoleEnumName(role);
            User user = populateUserToSave(userDto, isActive, isNonLocked, newUserRole);
            String tempPassword = userDto.getPassword();
            if(isBlank(tempPassword)) {
                tempPassword = generatePassword();
                user.setPassword(encoder.encode(tempPassword));
            }
            User savedUser = userRepository.save(user);
            emailService.sendAddedNewUserMessage(user.getFirstName(), generateActivationLink(user.getUserId()), tempPassword, user.getEmail());
            log.info("New user with email {} successfully added to database", savedUser.getEmail());
            return savedUser;
        } catch (NonTransientDataAccessException | MessagingException exc) {
            String message = String.format("An error occurred during attempt to add new user with email: %s", userDto.getEmail());
            log.error(message, exc);
            throw new BookStoreServiceException(message, exc);
        }
    }

    @Override
    public User updateUser(String userId, UserDto userDto, String role, boolean isNonLocked, boolean isActive) throws UserNotFoundException, UsernameExistsException, EmailExistsException {
        try {
            User currentUser = userRepository.findUserById(userId);
            if(currentUser==null) {
                String message = String.format("Attempt to update non existing currentUser with username: %s", userId);
                log.error(message);
                throw new BookStoreServiceException(message);
            }
            String newUsername = isBlank(userDto.getUsername()) ? userDto.getEmail() : userDto.getUsername();
            String currentUsername = isBlank(currentUser.getUsername()) ? currentUser.getEmail() : currentUser.getUsername();
            currentUser = validateNewUsernameAndEmail(currentUsername, newUsername, userDto.getEmail());
            Role newRole = getRoleEnumName(role);
            assert currentUser != null;
            User userToUpdate = populateUserToUpdate(currentUser, userDto, isActive, isNonLocked, newRole);
            log.info("Attempt to update user with email {}", userToUpdate.getEmail());
            return userRepository.save(userToUpdate);
        } catch (NonTransientDataAccessException exc) {
            String message = String.format("An error occurred during attempt to add new user with email: %s", userDto.getEmail());
            log.error(message, exc);
            throw new BookStoreServiceException(message, exc);
        }

    }

    @Override
    public void deleteUser(String username) throws UserNotFoundException {
        try {
            User userToDelete = userRepository.findUserByCredentials(username);
            if(isNull(userToDelete)) {
                throw new UserNotFoundException(String.format("Cannot find user with username: %s by attempt to delete the user"));
            }
            userRepository.deleteById(userToDelete.getId());
        } catch (NonTransientDataAccessException exc) {
            String message = String.format("An error occurred during attempt to delete user with username: %s", username);
            log.error(message, exc);
            throw new BookStoreServiceException(message, exc);
        }
    }

    @Override
    public void resetPassword(String email, String newPassword) throws UserNotFoundException, MessagingException {
        User userByEmail = userRepository.findUserByEmailIgnoreCase(email);
        if(userByEmail==null) {
            throw new UserNotFoundException("No user found by email: "+email);
        }
        userByEmail.setPassword(encoder.encode(newPassword));
        userByEmail.setActive(false);
        emailService.resetPasswordMessage(userByEmail.getFirstName(), generateActivationLink(userByEmail.getUserId()), userByEmail.getEmail());
        userRepository.save(userByEmail);
    }

    @Override
    public void activateUser(String userId) throws UserNotFoundException {
        User user = userRepository.findUserByUserId(userId);
        if(user==null) {
            throw new UserNotFoundException("No user found by userId: "+userId);
        }
        if(user.isActive()) {
            log.info("User with userId: {} is already active", userId);
            return;
        }
        user.setActive(true);
        userRepository.save(user);
    }

    @Override
    public Page<User> findUserByKeyword(String keyword, Integer pageNumber, Integer pageSize) {
        try {
            Pageable pageable = PageRequest.of(pageNumber, pageSize);
            return userRepository.findUserByKeyword(keyword, pageable);
        } catch (NonTransientDataAccessException exc) {
            String message = "An error occurred during attempt to find user by keyword.";
            log.error(message, exc);
            throw new BookStoreServiceException(message, exc);
        }
    }

    private String generateUserId() {
        return randomNumeric(15);
    }

    private String getRandomProfileImageUrl(UserDto userDto) {
        if(isNotBlank(userDto.getUsername())) {
            return profileImageSource+userDto.getUsername();
        }
        return profileImageSource+userDto.getEmail();
    }

    private User populateUserToSave(UserDto userDto, boolean isActive, boolean isNonLocked, Role role) {
        User user = new User(userDto);
        if(nonNull(userDto.getPassword())) {
            user.setPassword(encoder.encode(userDto.getPassword()));
        }
        user.setUserId(generateUserId());
        user.setActive(isActive);
        user.setNotLocked(isNonLocked);
        user.setRole(role.name());
        user.setAuthorities(role.getAuthorities());
        user.setProfileImageUrl(getRandomProfileImageUrl(userDto));
        if(userDto.getAddress()!=null) {
            saveUserAddress(userDto.getAddress(), user);
        }
        return user;
    }

    private User populateUserToUpdate(User currentUser, UserDto userDto, boolean isActive, boolean isNonLocked, Role role) {
        currentUser.setFirstName(userDto.getFirstName());
        currentUser.setLastName(userDto.getLastName());
        currentUser.setUsername(userDto.getUsername());
        currentUser.setEmail(userDto.getEmail());
        currentUser.setActive(isActive);
        currentUser.setNotLocked(isNonLocked);
        currentUser.setRole(role.name());
        currentUser.setAuthorities(role.getAuthorities());
        if(userDto.getAddress()!=null) {
            saveUserAddress(userDto.getAddress(), currentUser);
        }
        return currentUser;
    }

    private User validateNewUsernameAndEmail(String currentUsername, String newUsername, String email) throws UsernameExistsException, EmailExistsException, UserNotFoundException {
        if(isNotBlank(currentUsername)) {
            User currentUser = userRepository.findUserByCredentials(currentUsername);
            if(currentUser == null) {
                throw new UserNotFoundException("No username found by username: "+currentUsername);
            }
            User userByUsername = findUserByUsername(newUsername);
            if(userByUsername!=null && !currentUser.getId().equals(userByUsername.getId())) {
                throw new UsernameExistsException(USERNAME_ALREADY_TAKEN+newUsername);
            }
            User userByEmail = findUserByEmail(email);
            if(userByEmail!=null && !currentUser.getId().equals(userByEmail.getId())) {
                throw new EmailExistsException(EMAIL_ALREADY_HAS_ACCOUNT+email);
            }
            return currentUser;
        } else {
            if(userRepository.existsByUsername(newUsername)) {
                throw new UsernameExistsException(USERNAME_ALREADY_TAKEN+newUsername);
            }
            if(userRepository.existsByEmail(email)) {
                throw new EmailExistsException(EMAIL_ALREADY_HAS_ACCOUNT+email);
            }
            return null;
        }
    }

    private String generateActivationLink(String userId) {
        return appBaseLink+"aktywuj/"+userId;
    }

    private Role getRoleEnumName(String role) {
        return Role.valueOf("ROLE_"+role.toUpperCase());
    }

    private void saveUserAddress(AddressDto addressDto, User userToSave) {
        Address address = extractAddress(addressDto);
        if (isNull(address.getId())) {
            address.addUser(userToSave);
            addressRepository.save(address);
        } else {
            userToSave.setAddress(address);
        }
    }

    private Address extractAddress(AddressDto addressDto) {
        Optional<Address> addressOptional = addressRepository.findByStreetAndCityAndLocationNumberAndZipCodeIgnoreCase(
                addressDto.getStreet(),
                addressDto.getCity(),
                addressDto.getLocationNumber(),
                addressDto.getZipCode()
        );
        return addressOptional.orElseGet(() -> new Address(addressDto));
    }
}
