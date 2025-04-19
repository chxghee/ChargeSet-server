package com.chargeset.chargeset_server.repository.charging_station;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;

@RequiredArgsConstructor
public class ChargingStationCustomRepositoryImpl implements ChargingStationCustomRepository {

    private final MongoTemplate mongoTemplate;


}
