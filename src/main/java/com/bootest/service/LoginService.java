package com.bootest.service;

import com.bootest.model.User;
import com.bootest.repository.LoginRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class LoginService {

    private final LoginRepository loginRepository;

    /**
     * @return null이면 로그인 실패
     */
    public User login(String userId, String passwd) {

        return loginRepository.findByUserId(userId)
                .filter(m -> m.getPasswd().equals(passwd))
                .orElse(null);
    }
}
