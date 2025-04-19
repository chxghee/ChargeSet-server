package com.chargeset.chargeset_server.repository.evse;

import com.chargeset.chargeset_server.dto.charging_station.EvseStatusCount;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;


@ExtendWith(SpringExtension.class)
@SpringBootTest
//@TestPropertySource(locations = "classpath:application-test.yml") // 추후 테스트용 DB 분리시 사용
class EvseRepositoryTest {

    @Autowired
    private EvseRepository evseRepository;

    @Test
    public void 충전소_상태_조회_테스트() {
        // given
        List<EvseStatusCount> evseStatusCounts = evseRepository.countEvseStatus();

        Assertions.assertThat(evseStatusCounts.size()).isLessThan(5);
    }

}
