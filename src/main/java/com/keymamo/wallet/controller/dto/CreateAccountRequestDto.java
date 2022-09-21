package com.keymamo.wallet.controller.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateAccountRequestDto {

    private String password;

    @Builder
    public CreateAccountRequestDto(String password) {
        this.password = password;
    }
}
