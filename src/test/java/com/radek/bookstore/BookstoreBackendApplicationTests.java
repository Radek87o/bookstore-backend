package com.radek.bookstore;

import com.radek.bookstore.controller.BookController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yaml")
class BookstoreBackendApplicationTests {

	@Autowired
	BookController bookController;

	@MockBean
	AuthenticationManager authenticationManager;

	@Test
	void contextLoads() {}
}
