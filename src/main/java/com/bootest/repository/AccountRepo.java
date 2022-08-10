package com.bootest.repository;

import java.util.Optional;

import com.bootest.model.Account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AccountRepo extends JpaRepository<Account, String>, JpaSpecificationExecutor<Account> {

    Optional<Account> findByAccountId(String accountId);

    boolean existsByAccountId(String accountId);

}
