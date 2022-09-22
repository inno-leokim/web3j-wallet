package com.keymamo.wallet.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keymamo.wallet.controller.dto.*;
import com.keymamo.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.web3j.crypto.CipherException;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthSendTransaction;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Tag(name = "Wallet", description = "지갑 API")
@RequestMapping("/api/v1")
@RestController
public class WalletController {

    private final WalletService walletService;

    @Autowired
    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    /**
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @GetMapping("/block-number")
    public BlockNumberDto getBlockNumber()
            throws ExecutionException,
                    InterruptedException
    {
        BlockNumberDto blockNumberDto = new BlockNumberDto(walletService.getBlockNumber());
        return blockNumberDto;
    }


    /**
     * 함수명 : getEtherBalance
     * 내용 : 특정 계정의 이더리움 잔액체크
     *
     * @param address
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @GetMapping("/balance")
    public EthGetBalance getEtherBalance(
            @RequestParam(value = "address", required = true) String address
    ) throws
            ExecutionException,
            InterruptedException
    {
        return walletService.getEtherBalance(address);
//        return new EtherBalanceDto(walletService.getEtherBalance(address));
    }

    /**
     * 함수명 : getTransactionHistory
     * 내용 : 이더 전송 내역
     * @param address
     * @return
     * @throws JsonProcessingException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @GetMapping("/transaction/history")
    public ArrayList<HistoryResponseDto> getTransactionHistory(@RequestParam(value = "address", required = true) String address)
            throws ExecutionException, InterruptedException
    {
        return walletService.getTransactionHistory(address);
    }

    /**
     * 함수명 : createNewAccount
     * 내용 : 계정 생성 함수
     *
     * @param requestDto
     * @return
     * @throws InvalidAlgorithmParameterException
     * @throws CipherException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws NoSuchProviderException
     */
    @PostMapping("/wallet")
    public String createNewAccount(@RequestBody CreateAccountRequestDto requestDto)
            throws InvalidAlgorithmParameterException,
                    CipherException,
                    NoSuchAlgorithmException,
                    IOException,
                    NoSuchProviderException
    {
        return walletService.createAccount(requestDto);
    }

    /**
     * 함수명 : sendEtherTransaction
     * 내용 : 이더 전송
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws IOException
     */
    @PostMapping("/send/ether")
    public EthSendTransaction sendEtherTransaction(@RequestBody SendEtherRequestDto requestDto)
            throws ExecutionException,
                    InterruptedException,
                    IOException,
                    CipherException
    {
        return walletService.sendEtherTransaction(requestDto);
    }

}
