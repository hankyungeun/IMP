//rest컨트롤러 사용해본 거

// package com.bootest.controller;
//
// import com.bootest.dto.UserDto;
// import com.bootest.model.User;
// import com.bootest.repository.LoginRepository;
// import lombok.RequiredArgsConstructor;
// import org.springframework.ui.Model;
// import org.springframework.validation.BindingResult;
// import org.springframework.validation.annotation.Validated;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.web.servlet.ModelAndView;
//
// import java.util.UUID;
//
// @RestController
// @RequiredArgsConstructor
// @RequestMapping("/user")
// public class RegisterController {
//
// private final LoginRepository loginRepository;
//
// @GetMapping("/register")
// public ModelAndView findUsers(Model model) {
// model.addAttribute("userDto", new UserDto());
// ModelAndView mav = new ModelAndView();
// mav.setViewName("register");
// return mav;
// }
//
// @PostMapping("/register")
// public ModelAndView register(
// @ModelAttribute @Validated @RequestBody UserDto template,
// BindingResult bindingResult) {
//
// try {
// ModelAndView mav = new ModelAndView();
//
// if (bindingResult.hasErrors()) {
// mav.setViewName("register");
// return mav;
// }
//
// if (loginRepository.existsByUserId(template.getUserId())) {
// bindingResult.reject("RegistrationFailed", "이미 존재하는 ID입니다.");
// mav.setViewName("register");
// return mav;
// }
//
// User user = new User();
// user.setId(UUID.randomUUID().toString());
// user.setUserId(template.getUserId());
// user.setPasswd(template.getPasswd());
// user.setName(template.getName());
// user.setEmail(template.getEmail());
//
// loginRepository.save(user);
//
// mav.setViewName("index");
// return mav;
// } catch (Exception e) {
// throw e;
// }
// }
//
// }
