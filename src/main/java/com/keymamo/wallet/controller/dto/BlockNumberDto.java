package com.keymamo.wallet.controller.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;

@Getter
@RequiredArgsConstructor
public class BlockNumberDto {
    private final BigInteger blockNumber;
}
