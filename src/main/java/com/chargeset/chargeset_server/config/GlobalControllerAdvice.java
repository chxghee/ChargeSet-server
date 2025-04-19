package com.chargeset.chargeset_server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Value("${kakao.api.map-key}")
    private String kakaoMapKey;

    @ModelAttribute("kakaoApiKey")
    public String kakaoApiKey() {
        return kakaoMapKey;
    }
}
