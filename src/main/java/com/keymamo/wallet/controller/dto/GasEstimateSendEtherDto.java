package com.keymamo.wallet.controller.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;

@Getter
@RequiredArgsConstructor
public class GasEstimateSendEtherDto {
    private final BigDecimal estimatedGas;
}

