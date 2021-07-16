package com.radek.bookstore.utils.constants;

public class AuthorityConstants {
    public static final String[] USER_AUTHORITIES = { "comment:create", "rating:create", "order:read"};
    public static final String[] MODERATOR_AUTHORITIES = { "comment:create", "rating:create", "comment:delete",
                                                            "book:create", "book:update", "category:create",
                                                            "category:update", "author:update", "book:activate",
                                                            "book:delete", "order:read", "user:read", "user:update",
                                                            "user:activate", "user:lock"};
    public static final String[] ADMIN_AUTHORITIES = { "comment:create", "rating:create", "comment:delete",
                                                        "book:create", "book:update", "category:create",
                                                        "category:update", "author:update", "book:activate",
                                                        "book:delete", "order:read", "user:read", "user:update",
                                                        "user:activate", "user:lock", "user:delete"};
}
