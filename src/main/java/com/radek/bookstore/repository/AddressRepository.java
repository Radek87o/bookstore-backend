package com.radek.bookstore.repository;

import com.radek.bookstore.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, String> {

    Optional<Address> findByStreetAndCityAndLocationNumberAndZipCodeIgnoreCase(String street, String city, String locationNumber, String zipCode);
}
