package com.keymamo.wallet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlockNumber;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

@Service
public class WalletService {

    private final Web3j web3j;
    private EthBlockNumber blockNumber;

    @Autowired
    public WalletService(Web3j web3j) {
        this.web3j = web3j;
        this.blockNumber = new EthBlockNumber();
    }

    public BigInteger getBlockNumber() throws ExecutionException, InterruptedException {
        blockNumber = web3j.ethBlockNumber()
                .sendAsync()
                .get();

        return blockNumber.getBlockNumber();
    }
}
