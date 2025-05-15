package com.chargeset.chargeset_server.repository;

import com.chargeset.chargeset_server.document.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByIdToken(String idToken);
}
