package com.radek.bookstore.model;

import lombok.Getter;

import static com.radek.bookstore.utils.constants.AuthorityConstants.*;

@Getter
public enum Role {
    ROLE_USER(USER_AUTHORITIES),
    ROLE_MODERATOR(MODERATOR_AUTHORITIES),
    ROLE_ADMIN(ADMIN_AUTHORITIES);

    private String[] authorities;

    Role(String... authorities) {
        this.authorities = authorities;
    }
}
