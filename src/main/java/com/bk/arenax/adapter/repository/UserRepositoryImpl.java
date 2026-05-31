package com.bk.arenax.adapter.repository;

import com.bk.arenax.domain.user.User;
import com.bk.arenax.port.repository.UserRepository;
import com.bk.arenax.shared.pagination.BasePaginationRequest;
import com.bk.arenax.shared.pagination.PagedResult;
import com.bk.arenax.shared.pagination.PaginationHelper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

  private final JpaUserRepository jpaUserRepository;

  @Override
  public List<User> findAll() {
    return jpaUserRepository.findAll();
  }

  @Override
  public PagedResult<User> findAll(BasePaginationRequest request) {
    Page<User> page = jpaUserRepository.findAll(PaginationHelper.setPage(request));
    return PagedResult.of(
        page.getContent(),
        request.getCurrentPage(),
        request.getPageSize(),
        page.getTotalElements());
  }

  @Override
  public Optional<User> findById(Long id) {
    return jpaUserRepository.findById(id);
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return jpaUserRepository.findByEmail(email);
  }

  @Override
  public Boolean existsByEmail(String email) {
    return jpaUserRepository.existsUserByEmail(email);
  }

  @Override
  public Boolean existsById(Long userId){return jpaUserRepository.existsById(userId);}

  @Override
  public User save(User user) {
    return jpaUserRepository.save(user);
  }

  @Override
  public void deleteById(Long id) {
    jpaUserRepository.deleteById(id);
  }
}
