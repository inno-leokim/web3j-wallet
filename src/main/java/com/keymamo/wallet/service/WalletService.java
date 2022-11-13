package com.keymamo.wallet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.keymamo.wallet.controller.dto.*;
import jdk.jshell.JShell;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow;

@Getter
@RequiredArgsConstructor
class HistoryResult {
    private final String status;
    private final String message;
    private final List<Object> result;
}

@Service
public class WalletService {

    private final Web3j web3j;
    private String currentDirectory = System.getProperty("user.dir"); //현재 디렉토리
    private Integer etherSendGasUsed = 21000;

    @Value("${java.file.etherscan.apiUrl}") String etherScanApiUrl;
    @Value("${java.file.etherscan.apikey}") String etherScanApiKey;
    @Value("${java.file.networkId}") Integer networkId;

    @Value("${java.file.admin.address}") String adminAddress;
    @Value("${java.file.admin.privateKey}") String adminPrivateKey;


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

    public CreateAccountResponseDto createAccount(CreateAccountRequestDto requestDto)
            throws InvalidAlgorithmParameterException,
            CipherException,
            NoSuchAlgorithmException,
            IOException,
            NoSuchProviderException, ParseException {

        File walletFileDir = new File(currentDirectory + "/wallet-files");

        if(!walletFileDir.exists()) {
            walletFileDir.mkdirs();
        }

        String wallet = WalletUtils.generateNewWalletFile(
                requestDto.getPassword(),
                new File(currentDirectory + "/wallet-files"));
        String filePath = currentDirectory + "/wallet-files/" + wallet;
        Reader reader = new FileReader(filePath);

        JSONParser parser = new JSONParser();
        JSONObject jsonObj = (JSONObject)parser.parse(reader);
        String address = jsonObj.get("address").toString();

        return new CreateAccountResponseDto("0x"+address, filePath);
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
                        BigInteger.valueOf(etherSendGasUsed),
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

    public EthSendTransaction sendEtherTransactionByAdmin(SendEtherByAdminRequestDto requestDto)
            throws ExecutionException,
            InterruptedException,
            IOException
    {

        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(adminAddress, DefaultBlockParameterName.LATEST).send();

        RawTransaction rawTransaction =
                getRawTransaction(
                        ethGetTransactionCount.getTransactionCount(),
                        requestDto.getTo(),
                        BigInteger.valueOf(etherSendGasUsed),
                        web3j.ethGasPrice().sendAsync().get(),
                        Convert.toWei(requestDto.getAmount(), Convert.Unit.ETHER).toBigInteger()
                );

        Integer chainId = networkId; // networkID (1: mainnet, 3: ropstent)

        Credentials credentials = Credentials.create(adminPrivateKey);

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


    public List<HistoryResponseDto> getTransactionHistory(String address) throws ExecutionException, InterruptedException {

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

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = objectMapper.convertValue(responseEntity, Map.class);

        String status = "";
        String message = "";
        List<Object> result = new ArrayList<>();

        for(String key : map.keySet()) {
            if(key.equals("body")){
                Map<String, Object> resultMap = objectMapper.convertValue(map.get(key), Map.class);
                for (String resultKey : resultMap.keySet()) {
                    if(resultKey.equals("status")){
                        status = (String) resultMap.get(resultKey);
                    }

                    if(resultKey.equals("message")){
                        message = (String) resultMap.get(resultKey);
                    }

                    if(resultKey.equals("result")){
                        result = (ArrayList<Object>) resultMap.get(resultKey);
                    }
                }
            }
        }

        HistoryResult historyResult = new HistoryResult(status, message, result);
        HistoryResponseDto historyResponseDto = new HistoryResponseDto();
        ArrayList<HistoryResponseDto> historyResponseDtoArrayList = new ArrayList<>();

        Iterator it = result.iterator();

        while (it.hasNext()) {
            Map<String, Object> transactionMap = objectMapper.convertValue(it.next(), Map.class);

            for (String key : transactionMap.keySet()){
                switch (key) {
                    case "blockNumber"      : historyResponseDto.setBlockNumber((String) transactionMap.get(key)); break;
                    case "timeStamp"        : historyResponseDto.setTimeStamp((String) transactionMap.get(key)); break;
                    case "hash"             : historyResponseDto.setHash((String) transactionMap.get(key));  break;
                    case "nonce"            : historyResponseDto.setNonce((String) transactionMap.get(key)); break;
                    case "blockHash"        : historyResponseDto.setBlockHash((String) transactionMap.get(key)); break;
                    case "transactionIndex" : historyResponseDto.setTransactionIndex((String) transactionMap.get(key)); break;
                    case "from"             : historyResponseDto.setFrom((String) transactionMap.get(key)); break;
                    case "to"               : historyResponseDto.setTo((String) transactionMap.get(key)); break;
                    case "value"            : historyResponseDto.setValue((String) transactionMap.get(key)); break;
                    case "gas"              : historyResponseDto.setGas((String) transactionMap.get(key)); break;
                    case "gasPrice"         : historyResponseDto.setGasPrice((String) transactionMap.get(key)); break;
                    case "isError"          : historyResponseDto.setIsError((String) transactionMap.get(key)); break;
                    case "txreceipt_status" : historyResponseDto.setTxreceipt_status((String) transactionMap.get(key)); break;
                    case "input"            : historyResponseDto.setInput((String) transactionMap.get(key)); break;
                    case "contractAddress"  : historyResponseDto.setContractAddress((String) transactionMap.get(key)); break;
                    case "cumulativeGasUsed": historyResponseDto.setCumulativeGasUsed((String) transactionMap.get(key)); break;
                    case "gasUsed"          : historyResponseDto.setGasUsed((String) transactionMap.get(key)); break;
                    case "confirmations"    : historyResponseDto.setConfirmations((String) transactionMap.get(key)); break;
                    case "methodId"         : historyResponseDto.setMethodId((String) transactionMap.get(key)); break;
                    case "functionName"     : historyResponseDto.setFunctionName((String) transactionMap.get(key)); break;
                }
            }

            if(address.equals(Keys.toChecksumAddress(historyResponseDto.getFrom()))){
                historyResponseDto.setKind("send"); //이더 출금
            }

            if (address.equals(Keys.toChecksumAddress(historyResponseDto.getTo()))) {
                historyResponseDto.setKind("receive"); //이더 입금
            }

            // 이더 전송 시 gasUsed가 21000이기 때문에
            // 해당 조건에 맞는 건을 이더 전송 건으로 간주하고 추출
            if(historyResponseDto.getGasUsed().equals(etherSendGasUsed.toString())){
                historyResponseDtoArrayList.add(historyResponseDto);
            }

            historyResponseDto = new HistoryResponseDto(); //while을 돌기위해 다시 초기화
        }

        return historyResponseDtoArrayList;
    }

//    public Optional<TransactionReceipt> getTransactionReceipt(String transactionHash) throws IOException, TransactionException {
//
//        EthGetTransactionReceipt transactionReceipt = web3j.ethGetTransactionReceipt(transactionHash).send();
//
//        if (transactionReceipt.hasError()) {
//            throw new TransactionException("Error processing request: "
//                    + transactionReceipt.getError().getMessage());
//        }
//
//        return transactionReceipt.getTransactionReceipt();
//    }

    public String getTransactionStatus(String transactionHash) throws IOException, TransactionException {

        String status = "";
        EthGetTransactionReceipt transactionReceipt = web3j.ethGetTransactionReceipt(transactionHash).send();

        if (transactionReceipt.hasError()) {
//            throw new TransactionException("Error processing request: "
//                    + transactionReceipt.getError().getMessage());
            status = "pending";
            return status;
        }

        switch (transactionReceipt.getResult().getStatus()) {
            case "0x1":
                status = "success"; break;
            case "0x0":
                status = "fail"; break;
        }

        return status;
    }

    public BigDecimal getEstimatedGas() throws IOException {

        EthGasPrice ethGasPrice = web3j.ethGasPrice().send();

//        ethGasPrice.getGasPrice().multiply((BigInteger.valueOf(etherSendGasUsed)));
        BigInteger gasUsed = ethGasPrice.getGasPrice().multiply((BigInteger.valueOf(etherSendGasUsed)));

        return Convert.fromWei(gasUsed.toString(),Convert.Unit.ETHER);
    }

}

