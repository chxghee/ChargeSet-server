package com.chargeset.chargeset_server.repository.tou;

import com.chargeset.chargeset_server.document.Tou;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TouRepository extends MongoRepository<Tou, String> {

}
