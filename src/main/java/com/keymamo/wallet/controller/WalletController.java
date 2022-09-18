package com.keymamo.wallet.controller;

import com.keymamo.wallet.controller.dto.BlockNumberDto;
import com.keymamo.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.protocol.core.methods.response.EthBlockNumber;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

@RestController
public class WalletController {

    private final WalletService walletService;

    @Autowired
    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/api/v1/block-number")
    public BlockNumberDto getBlockNumber() throws ExecutionException, InterruptedException {

        BlockNumberDto blockNumberDto = new BlockNumberDto(walletService.getBlockNumber());

        return blockNumberDto;
    }
}
