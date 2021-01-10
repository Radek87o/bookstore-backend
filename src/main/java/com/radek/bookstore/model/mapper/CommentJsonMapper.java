package com.radek.bookstore.model.mapper;

import com.radek.bookstore.model.Comment;
import com.radek.bookstore.model.json.CommentJson;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.ConfigurableMapper;
import org.springframework.stereotype.Component;

@Component
public class CommentJsonMapper extends ConfigurableMapper {

    @Override
    protected void configure(MapperFactory factory) {
        factory.classMap(Comment.class, CommentJson.class)
                .byDefault()
                .register();
    }
}
