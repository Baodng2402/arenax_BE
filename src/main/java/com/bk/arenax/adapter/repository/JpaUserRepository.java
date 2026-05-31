package com.bk.arenax.adapter.repository;

import com.bk.arenax.domain.user.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface JpaUserRepository
    extends JpaRepository<User, Long>, QuerydslPredicateExecutor<User> {
  Boolean existsUserByEmail(String email);

  Optional<User> findByEmail(String email);
}
