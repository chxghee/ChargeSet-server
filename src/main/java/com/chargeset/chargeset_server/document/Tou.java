package com.chargeset.chargeset_server.document;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tou")
@Getter
public class Tou {

    @Id
    private String id;
    private String startTime;
    private String endTime;
    private String type;
    private double price_kWh;
    private double price_Wh;
}
