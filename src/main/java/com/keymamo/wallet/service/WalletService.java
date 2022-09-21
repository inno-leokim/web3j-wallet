package com.keymamo.wallet.service;

import com.keymamo.wallet.controller.dto.CreateAccountRequestDto;
import com.keymamo.wallet.controller.dto.SendEtherRequestDto;
import org.apache.tomcat.util.json.JSONParser;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class WalletService {

    private final Web3j web3j;
    private String currentDirectory = System.getProperty("user.dir"); //현재 디렉토리

    @Value("${java.file.etherscan.apiUrl}") String etherScanApiUrl;
    @Value("${java.file.etherscan.apikey}") String etherScanApiKey;
    @Value("${java.file.networkId}") Integer networkId;


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

    public String createAccount(CreateAccountRequestDto requestDto)
            throws InvalidAlgorithmParameterException,
                    CipherException,
                    NoSuchAlgorithmException,
                    IOException,
                    NoSuchProviderException {

        File walletFileDir = new File(currentDirectory + "/wallet-files");

        if(!walletFileDir.exists()) {
            walletFileDir.mkdirs();
        }

        return WalletUtils.generateNewWalletFile(
                requestDto.getPassword(),
                new File(currentDirectory + "/wallet-files")
        );
    }

    public EthGetBalance getEtherBalance(String address) throws ExecutionException, InterruptedException {

        EthGetBalance balance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST)
                                    .sendAsync()
                                    .get();

        return balance;
    }

    public EthSendTransaction sendEtherTransaction(SendEtherRequestDto requestDto)
            throws ExecutionException,
                    InterruptedException,
                    IOException,
                    CipherException
    {

        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(requestDto.getFrom(), DefaultBlockParameterName.LATEST).send();

        RawTransaction rawTransaction =
                getRawTransaction(
                        ethGetTransactionCount.getTransactionCount(),
                        requestDto.getTo(),
                        BigInteger.valueOf(21000),
                        web3j.ethGasPrice().sendAsync().get(),
                        Convert.toWei(requestDto.getAmount(), Convert.Unit.ETHER).toBigInteger()
                );

        Integer chainId = networkId; // networkID (1: mainnet, 3: ropstent)

        String addressToFindFile = requestDto.getFrom().replace("0x", ""); // from 주소에서 0x 제거

        File path = new File(currentDirectory + "/wallet-files");
        File fileList[] = path.listFiles();
        String fileName = "";

        for (File file : fileList) {

            if(file.getName().contains(addressToFindFile)){
                fileName = file.getName();
            }
        }

        Credentials credentials = WalletUtils.loadCredentials(requestDto.getPassword(), currentDirectory + "/wallet-files/"+fileName);

        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);
        String hexValue = Numeric.toHexString(signedMessage);
        EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
        
        return ethSendTransaction;
    }

    /**
     * 함수명 : getRawTransaction
     * 내용 : rawTransaction 생성
     * @param nonce
     * @param to
     * @param gasLimit
     * @param ethGasPrice
     * @param value
     * @return
     */
    @NotNull
    private static RawTransaction getRawTransaction(
            BigInteger nonce,
            String to,
            BigInteger gasLimit,
            EthGasPrice ethGasPrice,
            BigInteger value)
    {
        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                nonce,
                ethGasPrice.getGasPrice(),
                gasLimit,
                to,
                value
        );

        return rawTransaction;
    }


    public Object getTransactionHistory(String address) throws ExecutionException, InterruptedException {

        URI uri = UriComponentsBuilder
                .fromUriString(etherScanApiUrl)
                .path("/api")
                .queryParam("module", "account")
                .queryParam("action", "txlist")
                .queryParam("address", address)
                .queryParam("startblock", 0)
                .queryParam("endblock", getBlockNumber())
                .queryParam("offset", 10)
                .queryParam("sort", "asc")
                .queryParam("apikey", etherScanApiKey)
                .encode().build().toUri();

        RestTemplate restTemplete = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity entity = new HttpEntity("parameters", headers);

        ResponseEntity<Object> responseEntity = restTemplete.exchange(uri, HttpMethod.GET, entity, Object.class);

        return responseEntity;
    }
}
