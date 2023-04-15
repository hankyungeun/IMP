package com.bootest.controller;

import java.net.http.HttpRequest;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.bootest.model.ResultObject;
import com.bootest.web.SessionConstants;
import com.bootest.web.login.LoginForm;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.bootest.model.User;
import com.bootest.repository.LoginRepository;

import lombok.RequiredArgsConstructor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user-info")
public class UserInfoController {

    private final LoginRepository loginRepo;

    @GetMapping
    public ResultObject getUserData(HttpServletRequest request) {

        ResultObject result = new ResultObject();
        result.setData(request.getSession().getAttribute("loginUser"));

        return result;

    }

}
