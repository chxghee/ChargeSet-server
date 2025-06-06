package com.chargeset.chargeset_server.repository.reservation;

import com.chargeset.chargeset_server.document.Reservation;
import com.chargeset.chargeset_server.document.status.ReservationStatus;
import com.chargeset.chargeset_server.dto.EvseIdOnly;
import com.chargeset.chargeset_server.dto.reservation.ReservationInfoResponse;
import com.chargeset.chargeset_server.dto.reservation.ReservationNoShowCount;
import com.chargeset.chargeset_server.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.util.Pair;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class ReservationCustomRepositoryImpl implements ReservationCustomRepository {

    private final MongoTemplate mongoTemplate;

    /**
     * 1. 금일 시작하는 충전 예약 현황
     * 오늘 있는 예약들의 현황을 대시보드에 보여준다
     */
    public Page<ReservationInfoResponse> findTodayReservations(Pageable pageable) {
        Pair<Instant, Instant> todayRangeInKST = TimeUtils.getTodayRangeInKST();

        Criteria criteria = Criteria.where("startTime")
                .gte(todayRangeInKST.getFirst())
                .lt(todayRangeInKST.getSecond());
        MatchOperation match = Aggregation.match(criteria);

        SortOperation sort = Aggregation.sort(Sort.by(
                Sort.Order.asc("startTime"),
                Sort.Order.asc("_id")
        ));

        SkipOperation skip = Aggregation.skip(pageable.getOffset());
        LimitOperation limit = Aggregation.limit(pageable.getPageSize());

        AggregationOperation projectToLocalDate = getAggregationDateFormatingOperation();

        Aggregation aggregation = Aggregation.newAggregation(
                match,
                sort,
                skip,
                limit,
                projectToLocalDate
        );

        List<ReservationInfoResponse> reservation = mongoTemplate.aggregate(aggregation, "reservation", ReservationInfoResponse.class).getMappedResults();

        // total 개수 계산용
        long total = mongoTemplate.count(new Query(criteria), Reservation.class);       // 최적화 필요 - facet 로 전체 개수 쿼리도 조회 하기
        return new PageImpl<>(reservation, pageable, total);
    }

    /**
     * 2. 예약 검색 - 조회필터 : 충전소, 날짜, 예약 상태
     */
    public Page<ReservationInfoResponse> findReservationsWithFilter(LocalDate from, LocalDate to, String stationId,
                                                             ReservationStatus reservationStatus, Pageable pageable) {

        Criteria criteria = getSearchingConditionCriteria(from, to, stationId, reservationStatus);

        MatchOperation match = Aggregation.match(criteria);

        SortOperation sort = Aggregation.sort(Sort.by(
                Sort.Order.desc("startTime"),
                Sort.Order.asc("_id")  // Sort 안정성 - 페이징시 중복을 피하기 위해 키 필드 추가
        ));

        SkipOperation skip = Aggregation.skip(pageable.getOffset());
        LimitOperation limit = Aggregation.limit(pageable.getPageSize());

        AggregationOperation projectToLocalDate = getAggregationDateFormatingOperation();

        Aggregation aggregation = Aggregation.newAggregation(
                match,
                sort,
                skip,
                limit,
                projectToLocalDate
        );

        List<ReservationInfoResponse> reservation = mongoTemplate.aggregate(aggregation, "reservation", ReservationInfoResponse.class).getMappedResults();

        // total 개수 계산용
        long total = mongoTemplate.count(new Query(criteria), Reservation.class);
        return new PageImpl<>(reservation, pageable, total);
    }

    /**
     * 3. 충전소별 노쇼 / 완료 예약 조회
     */
    @Override
    public List<ReservationNoShowCount> getNoShowCounts(LocalDate from, LocalDate to) {

        Pair<Instant, Instant> utcRangeInKST = TimeUtils.getUTCRangeInKST(from, to);

        MatchOperation match = Aggregation.match(
                Criteria.where("reservationStatus").in("COMPLETED", "EXPIRED")
                        .and("startTime")
                        .gte(utcRangeInKST.getFirst())
                        .lte(utcRangeInKST.getSecond())
        );

        // 1차 그룹핑: 충전소 + 상태별
        GroupOperation group = Aggregation.group("stationId", "reservationStatus")
                .count().as("count");

        // 2차 그룹핑: 충전소 기준 + 조건별 분기 합산
        GroupOperation finalGroup = Aggregation.group("_id.stationId")
                .sum(ConditionalOperators
                        .when(Criteria.where("_id.reservationStatus").is("COMPLETED"))
                        .thenValueOf("count")
                        .otherwise(0)).as("completeCount")
                .sum(ConditionalOperators
                        .when(Criteria.where("_id.reservationStatus").is("EXPIRED"))
                        .thenValueOf("count")
                        .otherwise(0)).as("expiredCount");


        ProjectionOperation project = Aggregation.project()
                .and("_id").as("stationId")
                .andInclude("completeCount", "expiredCount");

        Aggregation aggregation = Aggregation.newAggregation(
                match,
                group,
                finalGroup,
                project
        );

        return mongoTemplate.aggregate(aggregation, "reservation", ReservationNoShowCount.class)
                .getMappedResults();
    }

    /**
     * 4. 엑셀용 한달간 예약 데이터 조회
     */
    @Override
    public List<ReservationInfoResponse> findAllReservationsByStationIdAndStartTime(String stationId, LocalDate from, LocalDate to) {

        Pair<Instant, Instant> utcRangeInKST = TimeUtils.getUTCRangeInKST(from, to);

        MatchOperation match = Aggregation.match(
                Criteria.where("startTime")
                        .gte(utcRangeInKST.getFirst())
                        .lte(utcRangeInKST.getSecond())
                        .and("stationId").is(stationId)
        );

        AggregationOperation projectToLocalDate = getAggregationDateFormatingOperation();

        SortOperation sort = Aggregation.sort(Sort.by(
                Sort.Order.desc("startTime"),
                Sort.Order.asc("_id")
        ));

        Aggregation aggregation = Aggregation.newAggregation(
                match,
                projectToLocalDate,
                sort
        );

        return mongoTemplate.aggregate(aggregation, "reservation", ReservationInfoResponse.class).getMappedResults();
    }


    /**
     * 5. 예약 가능한 evse 조회
     */
    @Override
    public List<EvseIdOnly> findOccupiedEvseIdsReservedAt(String stationId, LocalDateTime from, LocalDateTime to) {
        Pair<Instant, Instant> utcTimeRangeInKST = TimeUtils.getUTCTimeRangeInKST(from, to);

        // 1. 점유된 상황이려면
        // 일단 충전소 아이디 같아야함
        // 기존 예약 시작 시간 < 예약하려는 종료 시간
        // 기존 예약 종료 시간 > 예약하려는 시작 시간
        // 예약 상태 != CANCELED

        Criteria criteria = Criteria.where("stationId").is(stationId)
                .and("startTime").lte(utcTimeRangeInKST.getSecond())
                .and("endTime").gte(utcTimeRangeInKST.getFirst())
                .and("reservationStatus").in("ACTIVE", "WAITING", "ONGOING");

        MatchOperation match = Aggregation.match(criteria);

        GroupOperation group = Aggregation.group("evseId");

        ProjectionOperation project = Aggregation.project()
                .and("_id").as("evseId");

        Aggregation aggregation = Aggregation.newAggregation(
                match,
                group,
                project
        );

        return mongoTemplate.aggregate(aggregation, "reservation", EvseIdOnly.class)
                .getMappedResults();
    }


    @Override
    public Optional<ReservationInfoResponse> findCloseReservationByUserId(String userId) {

        Instant now = Instant.now();

        Criteria criteria = Criteria.where("userId").is(userId)
                .and("startTime").gte(now)
                .and("reservationStatus").in(ReservationStatus.WAITING, ReservationStatus.ACTIVE);

        MatchOperation match = Aggregation.match(criteria);

        AggregationOperation projectToLocalDate = getAggregationDateFormatingOperation();

        SortOperation sort = Aggregation.sort(Sort.by(
                Sort.Order.asc("startTime"),
                Sort.Order.asc("_id")
        ));

        LimitOperation limit = Aggregation.limit(1); // 첫 번째 문서만 가져오기

        Aggregation aggregation = Aggregation.newAggregation(
                match,
                projectToLocalDate,
                sort,
                limit
        );

        List<ReservationInfoResponse> reservation = mongoTemplate.aggregate(aggregation, "reservation", ReservationInfoResponse.class).getMappedResults();

        return reservation.stream().findFirst();
    }

    //== 메서드 ==//
    private static AggregationOperation getAggregationDateFormatingOperation() {
        // $project: 날짜 자료형 -> "yyyy-MM-dd-00:00" 문자열로 포맷 (KST 기준)
        AggregationOperation projectToLocalDate = context -> new Document("$project",
                new Document("startTime", new Document("$dateToString",
                        new Document("format", "%Y-%m-%d %H:%M")
                                .append("date", "$startTime")
                                .append("timezone", "Asia/Seoul")
                ))
                        .append("endTime", new Document("$dateToString",
                                new Document("format", "%Y-%m-%d %H:%M")
                                        .append("date", "$endTime")
                                        .append("timezone", "Asia/Seoul")
                        ))
                        .append("createdAt", new Document("$dateToString",
                                new Document("format", "%Y-%m-%d %H:%M")
                                        .append("date", "$createdAt")
                                        .append("timezone", "Asia/Seoul")
                        ))
                        .append("_id", 1)
                        .append("stationId", 1)
                        .append("evseId", 1)
                        .append("userId", 1)
                        .append("targetEnergyWh", 1)
                        .append("reservationStatus", 1)
        );
        return projectToLocalDate;
    }


    private static Criteria getSearchingConditionCriteria(LocalDate from, LocalDate to, String stationId, ReservationStatus reservationStatus) {
        Criteria criteria = new Criteria();

        if (from != null && to != null) {
            Pair<Instant, Instant> utcRangeInKST = TimeUtils.getUTCRangeInKST(from, to);

            criteria.and("startTime")
                    .gte(utcRangeInKST.getFirst())
                    .lt(utcRangeInKST.getSecond());
        }

        if (stationId != null) {
            criteria.and("stationId").is(stationId);
        }

        if (reservationStatus != null) {
            criteria.and("reservationStatus").is(reservationStatus);
        }

        return criteria;
    }
}

