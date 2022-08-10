//유저관리 = 회원가입컨트롤러
package com.bootest.controller;

import java.util.UUID;

import org.springframework.ui.Model;
import com.bootest.dto.*;
import com.bootest.model.*;
import com.bootest.repository.LoginRepository;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor

public class UserController {
    private final LoginRepository loginRepository;

    @GetMapping("/user")
    public String findUsers(Model model) {
        model.addAttribute("userDto", new UserDto());
        return "register";
    }

    @PostMapping("/user")
    public String registerUser(@ModelAttribute @Validated @RequestBody UserDto template,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        if (loginRepository.existsByUserId(template.getUserId())) {
            bindingResult.reject("RegistrationFailed", "이미 존재하는 ID입니다.");
            return "register";
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUserId(template.getUserId());
        user.setPasswd(template.getPasswd());
        user.setName(template.getName());
        user.setEmail(template.getEmail());

        loginRepository.save(user);

        return "redirect:/login";
    }

    @DeleteMapping("user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable(value = "id") String userId) {

        User user = loginRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 아이디 입니다."));

        loginRepository.delete(user);
        return ResponseEntity.ok().body("삭제가 완료되었습니다");

    }
}
