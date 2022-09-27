package com.keymamo.wallet.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@Configuration
@PropertySources({
        @PropertySource("classpath:properties/env.properties")
})
public class PropertyConfig {
    /**
     * env.properties 파일 사용을 위해 필요
     */
}
