package com.bk.arenax.port.repository;

import com.bk.arenax.domain.user.User;
import com.bk.arenax.shared.pagination.BasePaginationRequest;
import com.bk.arenax.shared.pagination.PagedResult;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
  List<User> findAll();

  PagedResult<User> findAll(BasePaginationRequest request);

  Optional<User> findById(Long id);

  Optional<User> findByEmail(String email);

  Boolean existsByEmail(String email);

  User save(User user);

  void deleteById(Long id);
}
