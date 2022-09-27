package com.keymamo.wallet.controller.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SendEtherByAdminRequestDto {

    private String to;     // 받는 계정
    private String amount; // 전송 수량

    @Builder
    public SendEtherByAdminRequestDto(String to, String amount) {

        this.to = to;
        this.amount = amount;

    }
}
