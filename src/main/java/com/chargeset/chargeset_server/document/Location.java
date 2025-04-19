package com.chargeset.chargeset_server.document;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class Location {
    private String address;
    private double lat;
    private double lng;
}
