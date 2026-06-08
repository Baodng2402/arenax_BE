package com.bk.arenax.adapter.repository;

import com.bk.arenax.domain.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface AccountRepository
    extends JpaRepository<Account, Long>, QuerydslPredicateExecutor<Account> {}
