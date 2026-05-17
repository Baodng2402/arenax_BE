package com.bk.arenax.adapter.repository;

import com.bk.arenax.domain.user.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserRepository extends JpaRepository<User, Long> {
    Boolean existsUserByEmail(String email);

    Optional<User> findByEmail(String email);

}
