package com.keymamo.wallet.controller.dto;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class CreateAccountResponseDto {
    private final String address;
    private final String file;
}
