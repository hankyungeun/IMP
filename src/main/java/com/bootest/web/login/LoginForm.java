package com.bootest.web.login;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class LoginForm {

    @NotBlank(message = "아이디는 필수 입니다")
    private String userId;

    @NotBlank(message = "비밀번호는 필수 입니다")
    private String passwd;
}