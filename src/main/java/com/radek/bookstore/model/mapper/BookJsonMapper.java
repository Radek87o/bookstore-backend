package com.radek.bookstore.model.mapper;

import com.radek.bookstore.model.Book;
import com.radek.bookstore.model.response.BookJson;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.ConfigurableMapper;
import org.springframework.stereotype.Component;

@Component
public class BookJsonMapper extends ConfigurableMapper {

    @Override
    protected void configure(MapperFactory factory) {
        factory.classMap(Book.class, BookJson.class)
                .exclude("description")
                .byDefault()
                .register();
    }
}
