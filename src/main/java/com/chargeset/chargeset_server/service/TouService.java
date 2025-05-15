package com.chargeset.chargeset_server.service;

import com.chargeset.chargeset_server.dto.TouInfoResponse;
import com.chargeset.chargeset_server.repository.tou.TouRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TouService {
    private final TouRepository touRepository;

    /**
     * 1. 전체 TOU 요금 단가 조회
     */
    public TouInfoResponse getTouInfo() {
        return new TouInfoResponse(touRepository.findAll());
    }

}
