package com.bootest.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class UserDto {
    @NotBlank(message = "아이디는 필수 입니다")
    private String userId;

    @NotBlank(message = "비밀번호는 필수 입니다")
    private String passwd;

    @NotBlank(message = "회원 이름은 필수 입니다")
    private String name;

    @NotBlank(message = "이메일은 필수 입니다")
    private String email;

}
