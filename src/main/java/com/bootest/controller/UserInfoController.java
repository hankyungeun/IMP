package com.bootest.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bootest.model.User;
import com.bootest.repository.LoginRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user-info")
public class UserInfoController {

    private final LoginRepository loginRepo;

    @GetMapping
    public List<User> getUserData() {
        return loginRepo.findAll();
    }

}
