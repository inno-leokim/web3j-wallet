package com.keymamo.wallet.controller.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SendEtherRequestDto {

    private String from;   // 보내는 계정
    private String password; // 보내는 계정의 패스워드
    private String to;     // 받는 계정
    private String amount; // 전송 수량

    @Builder
    public SendEtherRequestDto(String from, String password, String to, String amount) {
        this.from = from;
        this.password = password;
        this.to = to;
        this.amount = amount;
    }

}
