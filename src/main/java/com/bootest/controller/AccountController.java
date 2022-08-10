//키 등등 관리
package com.bootest.controller;

import java.util.UUID;

import com.bootest.dto.*;
import com.bootest.model.*;
import com.bootest.repository.AccountRepo;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor

public class AccountController {

    private final AccountRepo accountRepo;

    @GetMapping("/account")
    public String findAccount(Model model) {
        model.addAttribute("accountDto", new AccountDto());
        return "account";
    }

    @PostMapping("/account")
    public String registerAccount(@ModelAttribute @Validated @RequestBody AccountDto template,
                               BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "account";
        }

        if (accountRepo.existsByAccountId(template.getAccountId())) {
            bindingResult.reject("RegistrationFailed", "이미 존재하는 계정입니다.");
            return "account";
        }

        Account account = new Account();
        account.setId(UUID.randomUUID().toString());
        account.setAccountId(template.getAccountId());
        account.setUserId(template.getUserId());
        account.setName(template.getName());
        account.setRegions(template.getRegions());
        account.setAccessKey(template.getAccessKey());
        account.setSecretKey(template.getSecretKey());

        accountRepo.save(account);

        return "index";
    }

    @DeleteMapping("account/{id}")
    public ResponseEntity<?> deleteAccount(@PathVariable(value = "id") String accountId) {

        Account account = accountRepo.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 아이디 입니다."));

        accountRepo.delete(account);
        return ResponseEntity.ok().body("삭제가 완료되었습니다");

    }

}
