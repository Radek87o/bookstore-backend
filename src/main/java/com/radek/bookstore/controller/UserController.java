package com.radek.bookstore.controller;

import com.radek.bookstore.model.User;
import com.radek.bookstore.model.UserPrincipal;
import com.radek.bookstore.model.dto.LoginDto;
import com.radek.bookstore.model.dto.ResetPasswordDto;
import com.radek.bookstore.model.dto.UserDto;
import com.radek.bookstore.model.exception.EmailExistsException;
import com.radek.bookstore.model.exception.UserNotFoundException;
import com.radek.bookstore.model.exception.UsernameExistsException;
import com.radek.bookstore.model.response.HttpResponse;
import com.radek.bookstore.security.utility.JwtTokenProvider;
import com.radek.bookstore.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.validation.Valid;

import static com.radek.bookstore.utils.constants.SecurityConstants.JWT_TOKEN_HEADER;
import static java.util.Objects.isNull;
import static org.springframework.http.HttpStatus.OK;

@CrossOrigin
@RestController
@RequestMapping("/api/users")
public class UserController {

    public static final String EMAIL_WITH_NEW_PASSWORD_SENT = "An email with a new password sent to: ";

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public UserController(UserService userService,
                          AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDto user)
            throws UserNotFoundException, UsernameExistsException, EmailExistsException {
        User registeredUser = userService.registerUser(user);
        return ResponseHelper.createCreatedResponse(registeredUser);
    }

    @PostMapping("/signin")
    public ResponseEntity<User> loginUser(@Valid @RequestBody LoginDto login) throws UserNotFoundException {
        authenticateUser(login);
        User loginUser = userService.findUserByUsernameOrEmail(login.getUsername());
        UserPrincipal userPrincipal = new UserPrincipal(loginUser);
        HttpHeaders jwtHeader = createJwtHeader(userPrincipal);
        return new ResponseEntity<>(loginUser, jwtHeader, OK);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('user:read')")
    public ResponseEntity<?> findUsers(@RequestParam(value = "page", required = false) Integer page,
                                       @RequestParam(value = "size", required = false) Integer size) {
        page = isNull(page) ? 0 : page;
        size = isNull(size) ? 25 : size;
        Page<User> users = userService.findAllUsers(page, size);
        return ResponseHelper.createOkResponse(users);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('user:update')")
    public ResponseEntity<?> addNewUser(@Valid @RequestBody UserDto userDto,
                                        @RequestParam(name = "isActive", required = false, defaultValue = "true") String isActive,
                                        @RequestParam(name = "isNonLocked", required = false, defaultValue = "true") String isNonLocked,
                                        @RequestParam(name = "role") String role) throws UserNotFoundException, UsernameExistsException, EmailExistsException {
        User newUser = userService.addNewUser(userDto, role, Boolean.parseBoolean(isActive), Boolean.parseBoolean(isNonLocked));
        return ResponseHelper.createCreatedResponse(newUser);
    }

    @PostMapping(path="/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('user:update')")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserDto userDto,
                                        @RequestParam(name = "currentUsername") String currentUsername,
                                        @RequestParam(name = "isActive", required = false, defaultValue = "true") String isActive,
                                        @RequestParam(name = "isNonLocked", required = false, defaultValue = "true") String isNonLocked,
                                        @RequestParam(name = "role") String role) throws UserNotFoundException, UsernameExistsException, EmailExistsException {
        User updatedUser = userService.updateUser(currentUsername, userDto, role, Boolean.parseBoolean(isActive), Boolean.parseBoolean(isNonLocked));
        return ResponseHelper.createOkResponse(updatedUser);
    }

    @GetMapping(path = "/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('user:read')")
    public ResponseEntity<?> findUser(@PathVariable("username") String username) throws UserNotFoundException {
        User user = userService.findUserByUsernameOrEmail(username);
        return ResponseHelper.createOkResponse(user);
    }

    @PostMapping(path = "/resetPassword", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordDto resetPasswordDto) throws UserNotFoundException, MessagingException {
        userService.resetPassword(resetPasswordDto.getEmail(), resetPasswordDto.getPassword());
        return response(OK,EMAIL_WITH_NEW_PASSWORD_SENT+resetPasswordDto.getEmail());
    }

    @DeleteMapping("/{username}")
    @PreAuthorize("hasAnyAuthority('user:delete')")
    public ResponseEntity<HttpResponse> deleteUser(@PathVariable("username") String username) throws UserNotFoundException {
        userService.deleteUser(username);
        return response(OK, String.format("User with username: %s successfully deleted", username));
    }

    @GetMapping("/activate")
    public ResponseEntity<HttpResponse> activateUser(@RequestParam("userId") String userId) throws UserNotFoundException {
        userService.activateUser(userId);
        return response(OK, String.format("Account of the user with id: %s successfully activated", userId));
    }

    private void authenticateUser(LoginDto login) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword()));
    }

    private HttpHeaders createJwtHeader(UserPrincipal userPrincipal) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(JWT_TOKEN_HEADER, jwtTokenProvider.generateJwtToken(userPrincipal));
        return headers;
    }

    private ResponseEntity<HttpResponse> response(HttpStatus status, String message) {
        HttpResponse httpResponse = new HttpResponse(status.value(), status, status.getReasonPhrase().toUpperCase(), message);
        return new ResponseEntity(httpResponse, status);
    }
}
