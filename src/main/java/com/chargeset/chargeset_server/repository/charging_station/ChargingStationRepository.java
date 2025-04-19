package com.chargeset.chargeset_server.repository.charging_station;

import com.chargeset.chargeset_server.document.ChargingStation;
import com.chargeset.chargeset_server.dto.charging_station.ChargingStationInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChargingStationRepository extends MongoRepository<ChargingStation, String>, ChargingStationCustomRepository {

    @Query(value = "{}", fields = "{ 'id' :  1, 'stationId' :  1, 'name' : 1, 'location': 1}")
    List<ChargingStationInfo> findAllWithLocation();

    // ?0, ?1 등은 메서드 파라미터 인덱스를 의미 (첫 번째, 두 번째…)
    @Query(value = "{ 'stationId': ?0 }", fields = "{ 'id' :  1, 'stationId' :  1, 'name' : 1, 'location': 1}")
    ChargingStationInfo findByStationId(String stationId);

}
