package com.chargeset.chargeset_server.repository.evse;

import com.chargeset.chargeset_server.document.Evse;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EvseRepository extends MongoRepository<Evse, String>, EvseCustomRepository {
}
