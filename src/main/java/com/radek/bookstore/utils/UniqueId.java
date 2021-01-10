package com.radek.bookstore.utils;

import java.util.UUID;

public class UniqueId {

    public static String nextId(){
        return UUID.randomUUID().toString();
    }
}
