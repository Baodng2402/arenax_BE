package com.bk.arenax.adapter.repository;

import com.bk.arenax.domain.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaAccountRepository extends JpaRepository<Account, Long> {}
