package com.keymamo.wallet.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Configuration
public class Web3Config {

    @Bean
    public Web3j web3j () {
        return Web3j.build(new HttpService("https://ropsten.infura.io/v3/24ef2d9c3e174f86a26b5aec0d2cd71e"));
    }
}
