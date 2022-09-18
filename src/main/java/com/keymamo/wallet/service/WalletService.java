package com.keymamo.wallet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGetBalance;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.concurrent.ExecutionException;

@Service
public class WalletService {

    private final Web3j web3j;

    @Autowired
    public WalletService(Web3j web3j) {
        this.web3j = web3j;
    }

    public BigInteger getBlockNumber() throws ExecutionException, InterruptedException {

        EthBlockNumber blockNumber = new EthBlockNumber();

        blockNumber = web3j.ethBlockNumber()
                .sendAsync()
                .get();

        return blockNumber.getBlockNumber();
    }

    public String createAccount() throws InvalidAlgorithmParameterException, CipherException, NoSuchAlgorithmException, IOException, NoSuchProviderException {
        return WalletUtils.generateNewWalletFile(
                "1234",
                new File("/Users/brand13/Dev/java-study/wallet/accounts-files")
        );
    }

    public BigInteger getEtherBalance(String address) throws ExecutionException, InterruptedException {
        EthGetBalance balance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST)
                                    .sendAsync()
                                    .get();

        return balance.getBalance();
    }
}
