package com.keymamo.wallet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
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

        String currentDirectory = System.getProperty("user.dir"); //현재 디렉토리

        File walletFileDir = new File(currentDirectory + "/wallet-files");

        if(!walletFileDir.exists()) {
            walletFileDir.mkdirs();
        }

        return WalletUtils.generateNewWalletFile(
                "랜덤비번",
                new File(currentDirectory + "/wallet-files")
        );
    }

    public BigInteger getEtherBalance(String address) throws ExecutionException, InterruptedException {
        EthGetBalance balance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST)
                                    .sendAsync()
                                    .get();

        return balance.getBalance();
    }

    public String sendEtherTransaction() throws ExecutionException, InterruptedException, IOException {

        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount("0xF4ed389d4A73D9D87A4d5f1506b04a1c284E0de3",DefaultBlockParameterName.LATEST).send();
        BigInteger nonce = ethGetTransactionCount.getTransactionCount();

        String to = ""; // receiver의 address
        Integer chainId = 3; // networkID (1: mainnet, 3: ropstent)
        BigInteger gasLimit = BigInteger.valueOf(21000);
        EthGasPrice ethGasPrice = web3j.ethGasPrice().sendAsync().get();
        BigInteger value = Convert.toWei("0.2", Convert.Unit.ETHER).toBigInteger();

        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                nonce,
                ethGasPrice.getGasPrice(),
                gasLimit,
                to,
                value
        );


        String privateKey=""; // signer의 private_key. 파일에서 추출한다.

        Credentials credentials = Credentials.create(privateKey);

        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);

        String hexValue = Numeric.toHexString(signedMessage);

        EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
        String transactionHash = ethSendTransaction.getTransactionHash();

        return transactionHash;
    }
}
