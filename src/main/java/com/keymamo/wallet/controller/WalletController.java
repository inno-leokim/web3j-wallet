package com.keymamo.wallet.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.keymamo.wallet.controller.dto.*;
import com.keymamo.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.web3j.crypto.CipherException;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.exceptions.TransactionException;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.List;
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
     * 함수명 : getBlockNumber
     * 내용 : 현재 block number 조회 함수
     *
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @GetMapping("/block-number")
    public BlockNumberDto getBlockNumber()
            throws ExecutionException,
            InterruptedException {
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
            InterruptedException {
        return walletService.getEtherBalance(address);
//        return new EtherBalanceDto(walletService.getEtherBalance(address));
    }

    /**
     * 함수명 : getTransactionHistory
     * 내용 : 이더 전송 내역
     *
     * @param address
     * @return
     * @throws JsonProcessingException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @GetMapping("/transaction/history")
    public List<HistoryResponseDto> getTransactionHistory(@RequestParam(value = "address", required = true) String address)
            throws ExecutionException, InterruptedException {
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
    public CreateAccountResponseDto createNewAccount(@RequestBody CreateAccountRequestDto requestDto)
            throws InvalidAlgorithmParameterException,
            CipherException,
            NoSuchAlgorithmException,
            IOException,
            NoSuchProviderException, ParseException {
        return walletService.createAccount(requestDto);
    }

    /**
     * 함수명 : sendEtherTransaction
     * 내용 : 이더 전송
     *
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
            CipherException {
        return walletService.sendEtherTransaction(requestDto);
    }

    /**
     * 함수명 : sendEtherTransactionByAdmin
     * 내용 : 관리자 이더 전송
     *
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws IOException
     */
    @PostMapping("/send/ether/admin")
    public EthSendTransaction sendEtherTransactionByAdmin(@RequestBody SendEtherByAdminRequestDto requestDto)
            throws ExecutionException,
            InterruptedException,
            IOException,
            CipherException {
        return walletService.sendEtherTransactionByAdmin(requestDto);
    }

    /**
     * 함수명 : EthGetTransactionReceipt
     * 내용 : transaction 상태
     *
     * @param transactionHash
     * @return
     * @throws TransactionException
     * @throws IOException
     */
//    @GetMapping("/transaction/status")
//    public Optional<TransactionReceipt> getTransactionReceipt(@RequestParam(value = "transactionHash", required = true) String transactionHash) throws TransactionException, IOException {
//        return walletService.getTransactionReceipt(transactionHash);
//    }

    /**
     * 함수명 : getEstimatedGas
     * 내용 : 이더 전송에 소모될 추측 가스량 리턴(단위 : 이더)
     * @return
     * @throws IOException
     */
    @GetMapping("/estimate/gas/sendEth")
    public GasEstimateSendEtherDto getEstimatedGas() throws IOException {
        GasEstimateSendEtherDto gasEstimateSendEther = new GasEstimateSendEtherDto(walletService.getEstimatedGas());
        return gasEstimateSendEther;
    }
}
