package com.bootest.controller;


import com.bootest.model.Account;
import com.bootest.model.User;
import com.bootest.web.SessionConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequiredArgsConstructor
public class PageController {


    @GetMapping("/instanceUsage")
    public String instanceUsage(@SessionAttribute(name = SessionConstants.LOGIN_USER, required = false) User loginUser,
                                Model model) {
        model.addAttribute("user", loginUser);
        return "instance";
    }

    @GetMapping("/recommendation")
    public String recommendation(@SessionAttribute(name = SessionConstants.LOGIN_USER, required = false) User loginUser,
                                 Model model) {
        model.addAttribute("user", loginUser);
        return "recommendation";
    }

}
