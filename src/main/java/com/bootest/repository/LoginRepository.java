package com.bootest.repository;

import java.util.Optional;
import com.bootest.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LoginRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {
    Optional<User> findByUserId(String userId);

    boolean existsByUserId(String userId);

//    Optional<User> findByPasswd(String passwd);
//
//    boolean existsByPasswd(String passwd);
}
