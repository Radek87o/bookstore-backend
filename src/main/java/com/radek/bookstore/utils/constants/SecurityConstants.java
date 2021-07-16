package com.radek.bookstore.utils.constants;

public class SecurityConstants {

    public static final long EXPIRATION_TIME = 432_000; //5 days expressed in seconds
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String JWT_TOKEN_HEADER = "Jwt-Token";
    public static final String TOKEN_CANNOT_BE_VERIFIED = "Token cannot be verified";
    public static final String RADEK_COM = "radek.com";
    public static final String RADEK_COM_ADMINISTRATION = "Bookstore - Radosław Ornat";
    public static final String AUTHORITIES = "authorities";
    public static final String FORBIDDEN_MESSAGE = "You need to login to access this page";
    public static final String ACCESS_DENIED_MESSAGE = "You do not have permission to access this resource";
    public static final String OPTIONS_HTTP_METHOD = "OPTIONS";
    public static final String[] PUBLIC_URLS = {
                                        "/api/users/signup",
                                        "/api/users/signin",
                                        "/api/users/activate/**",
                                        "/api/checkout/purchase"
                                    };
    public static final String[] PUBLIC_GET_URLS = {
                                        "/api/authors/**",
                                        "/api/books/**",
                                        "/api/categories/**",
                                        "/api/ratings/**",
                                        "/api/comments/**"
                                    };
}
