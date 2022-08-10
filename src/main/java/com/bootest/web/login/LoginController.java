package com.bootest.web.login;

import com.bootest.service.LoginService;
import com.bootest.model.User;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpSession;

import javax.servlet.http.HttpServletRequest;
import com.bootest.web.*;
import org.springframework.web.bind.annotation.SessionAttribute;

@RequiredArgsConstructor
@Controller
public class LoginController {

    private final LoginService loginService;

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("loginForm", new LoginForm());
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @SessionAttribute(name = SessionConstants.LOGIN_USER, required = false) @ModelAttribute @Validated LoginForm loginForm,
            BindingResult bindingResult, HttpServletRequest request,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "login";
        }

        User loginUser = loginService.login(loginForm.getUserId(), loginForm.getPasswd());

        if (loginUser == null) {
            bindingResult.reject("loginFail", "아이디 또는 비밀번호가 맞지 않습니다.");
            return "login";
        }

        // 로그인 성공 처리
        HttpSession session = request.getSession(); // 세션이 있으면 있는 세션 반환, 없으면 신규 세션을
        // 생성하여 반환
        session.setAttribute(SessionConstants.LOGIN_USER, loginUser); // 세션에 로그인 회원
        // 정보 보관

        model.addAttribute("user", loginUser);

        return "index";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // 세션 날림
        }

        return "redirect:/";
    }

}