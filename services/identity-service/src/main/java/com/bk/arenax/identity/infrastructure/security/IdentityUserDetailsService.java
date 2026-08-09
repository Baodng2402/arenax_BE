package com.bk.arenax.identity.infrastructure.security;

import com.bk.arenax.identity.domain.UserIdentifierType;
import com.bk.arenax.identity.repository.UserIdentifierRepository;
import com.bk.arenax.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class IdentityUserDetailsService implements UserDetailsService {

  private final UserIdentifierRepository userIdentifierRepository;
  private final UserRepository userRepo;

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    String normalizedEmail = Objects.requireNonNull(username).trim().toLowerCase(Locale.ROOT);
    var identifier = userIdentifierRepository
            .findByTypeAndNormalizedValue(UserIdentifierType.EMAIL, normalizedEmail)
            .orElseThrow(() -> new UsernameNotFoundException("email not exist"));
    var user = userRepo.findById(identifier.getUserId())
            .orElseThrow(() -> new UsernameNotFoundException("email not exist"));
    return new IdentityUserDetails(user, identifier);
  }


}
