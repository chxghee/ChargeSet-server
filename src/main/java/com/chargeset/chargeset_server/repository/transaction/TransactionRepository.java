package com.chargeset.chargeset_server.repository.transaction;

import com.chargeset.chargeset_server.document.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TransactionRepository extends MongoRepository<Transaction, String>, TransactionCustomRepository {
}
