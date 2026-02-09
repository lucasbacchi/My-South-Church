package com.southchurch.my.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.southchurch.my.models.Role;


public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
