package com.keymamo.wallet.controller;

import com.keymamo.wallet.controller.dto.BlockNumberDto;
import com.keymamo.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.crypto.CipherException;
import org.web3j.protocol.core.methods.response.EthBlockNumber;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.concurrent.ExecutionException;

@RestController
public class WalletController {

    private final WalletService walletService;

    @Autowired
    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    /**
     *
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @GetMapping("/api/v1/block-number")
    public BlockNumberDto getBlockNumber() throws ExecutionException, InterruptedException {

        BlockNumberDto blockNumberDto = new BlockNumberDto(walletService.getBlockNumber());

        return blockNumberDto;
    }

    /**
     * 함수명 : createNewAccount
     * 내용 : 계정 생성 함수
     * @return
     * @throws InvalidAlgorithmParameterException
     * @throws CipherException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws NoSuchProviderException
     */
    @PostMapping("/api/v1/wallet")
    public String createNewAccount() throws InvalidAlgorithmParameterException, CipherException, NoSuchAlgorithmException, IOException, NoSuchProviderException {
        return walletService.createAccount();
    }

    /**
     * 함수명 : getEtherBalance
     * 내용 : 특정 계정의 이더리움 잔액체크
     * @param address
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @GetMapping("/api/v1/balance/{address}")
    public BigInteger getEtherBalance(@PathVariable(value = "address", required = true) String address) throws ExecutionException, InterruptedException {
        return walletService.getEtherBalance(address);
    }
}
