package com.chargeset.chargeset_server.dto.tansaction;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UsageBucketResult {
    private String id;
    private long userCount;
}
