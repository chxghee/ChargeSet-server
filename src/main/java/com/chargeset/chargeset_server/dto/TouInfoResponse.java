package com.chargeset.chargeset_server.dto;

import com.chargeset.chargeset_server.document.Tou;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TouInfoResponse {
    private List<Tou> touData;
}
