package com.chargeset.chargeset_server.repository;

import com.chargeset.chargeset_server.document.ChargingProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChargingProfileRepository extends MongoRepository<ChargingProfile, String> {
}
