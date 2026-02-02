package com.southchurch.my.repositories;

import com.southchurch.my.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// This magic interface gives you .save(), .findAll(), .delete(), etc. for free!
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
