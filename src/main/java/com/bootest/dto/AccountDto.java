package com.bootest.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class AccountDto {
    @NotBlank(message = "계정명은 필수 입니다")
    private String accountId;

    @NotBlank(message = "아이디는 필수 입니다")
    private String userId;

    @NotBlank(message = "이름은 필수 입니다")
    private String name;

    private String regions;

    @NotBlank(message = "키입력은 필수 입니다")
    private String accessKey;

    @NotBlank(message = "키입력은 필수 입니다")
    private String secretKey;
}
