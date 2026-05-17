package com.bk.arenax.port.repository;

import com.bk.arenax.domain.user.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    List<User> findAll();

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    User save(User user);

    void deleteById(Long id);
}
