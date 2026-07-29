package com.bk.arenax.identity.infrastructure.security;

import com.bk.arenax.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class IdentityUserDetailsService implements UserDetailsService {

  private final UserRepository userRepo;

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    var user = userRepo
            .findByEmail(Objects.requireNonNull(username.trim()))
            .orElseThrow(()->new UsernameNotFoundException(
            "email not exist"));
    return new IdentityUserDetails(user);
  }


}
