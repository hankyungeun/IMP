package com.bootest.web;

import com.bootest.model.*;
import com.bootest.repository.RecoVolumeRepo;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final RecoVolumeRepo recoVolumeRepo;

    @GetMapping("/")
    public String home(@SessionAttribute(name = SessionConstants.LOGIN_USER, required = false) User loginUser,
            Model model, Account account) {
        // 세션에 회원 데이터가 없으면 홈으로 이동
        if (loginUser == null) {
            return "home";
        }

        // 세션이 유지되면 로그인 홈으로 이동
        model.addAttribute("user", loginUser);
        model.addAttribute("volumes", (recoVolumeRepo.findByVolumeId(account.getAccountId())));

        return "index";
    }
}