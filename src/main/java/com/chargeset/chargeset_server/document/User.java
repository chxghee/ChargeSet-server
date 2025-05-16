package com.chargeset.chargeset_server.document;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user")
@Getter
public class User {

    @Id
    private String id;
    private String email;
    private String password;
    private String name;
    private String idToken;
}
