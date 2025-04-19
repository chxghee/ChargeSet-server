package com.chargeset.chargeset_server.repository.reservation;

import com.chargeset.chargeset_server.document.Reservation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReservationRepository extends MongoRepository<Reservation, String>, ReservationCustomRepository {
}
