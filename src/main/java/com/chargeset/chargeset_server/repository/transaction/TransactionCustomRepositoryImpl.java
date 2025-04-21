package com.chargeset.chargeset_server.repository.transaction;

import com.chargeset.chargeset_server.document.Transaction;
import com.chargeset.chargeset_server.document.status.TransactionStatus;
import com.chargeset.chargeset_server.dto.tansaction.ChargingDailyStat;
import com.chargeset.chargeset_server.dto.tansaction.ChargingHourlyStat;
import com.chargeset.chargeset_server.dto.tansaction.ChargingProfileResponse;
import com.chargeset.chargeset_server.dto.tansaction.TransactionInfoResponse;
import com.chargeset.chargeset_server.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
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
import java.util.*;

@RequiredArgsConstructor
public class TransactionCustomRepositoryImpl implements TransactionCustomRepository {

    private final MongoTemplate mongoTemplate;

    /**
     * 1. 오늘 하루의 전체 매출 현황 조회
     */
    @Override
    public Optional<ChargingDailyStat> getTodayChargingStats() {
        Pair<Instant, Instant> todayRangeInKST = TimeUtils.getTodayRangeInKST();

        MatchOperation match = getTimeRangeMatchOperation(todayRangeInKST);

        AggregationOperation projectToLocalDate = getAggregationDateFormatingOperation();

        GroupOperation group = Aggregation.group("date")
                .sum("cost").as("totalRevenue")
                .sum("energyWh").as("totalEnergy")
                .count().as("count");

        ProjectionOperation finalProject = Aggregation.project()
                .and("_id").as("date")
                .andInclude("totalRevenue", "totalEnergy", "count");

        Aggregation aggregation = Aggregation.newAggregation(match, projectToLocalDate, group, finalProject);

        AggregationResults<ChargingDailyStat> result = mongoTemplate.aggregate(aggregation, "transaction", ChargingDailyStat.class);

        return Optional.ofNullable(result.getUniqueMappedResult());
    }

    /**
     * 2. 충전소볗 오늘 하루의 매출 현황 조회
     */
    @Override
    public Optional<ChargingDailyStat> getTodayChargingStatsByStationId(String stationId) {
        Pair<Instant, Instant> todayRangeInKST = TimeUtils.getTodayRangeInKST();

        MatchOperation match = getStationAndTimeRangeMatchOperation(todayRangeInKST, stationId);

        AggregationOperation projectToLocalDate = getAggregationDateFormatingOperation();

        GroupOperation group = Aggregation.group("date")
                .sum("cost").as("totalRevenue")
                .sum("energyWh").as("totalEnergy")
                .count().as("count");

        ProjectionOperation finalProject = Aggregation.project()
                .and("_id").as("date")
                .andInclude("totalRevenue", "totalEnergy", "count");

        Aggregation aggregation = Aggregation.newAggregation(match, projectToLocalDate, group, finalProject);

        AggregationResults<ChargingDailyStat> result = mongoTemplate.aggregate(aggregation, "transaction", ChargingDailyStat.class);

        return Optional.ofNullable(result.getUniqueMappedResult());
    }

    /**
     * 3. 일주일의 매출 현황 조회
     */
    @Override
    public List<ChargingDailyStat> getWeeklyChargingStats() {

        Pair<Instant, Instant> weeklyRangeInKST = TimeUtils.getWeeklyRangeInKST();

        MatchOperation match = getTimeRangeMatchOperation(weeklyRangeInKST);

        AggregationOperation projectToLocalDate = getAggregationDateFormatingOperation();

        GroupOperation groupOperation = Aggregation.group("date")
                .sum("cost").as("totalRevenue")
                .sum("energyWh").as("totalEnergy")
                .count().as("count");

        ProjectionOperation finalProject = Aggregation.project()
                .and("_id").as("date")
                .andInclude("totalRevenue", "totalEnergy", "count");

        Aggregation aggregation = Aggregation.newAggregation(
                match,
                projectToLocalDate,
                groupOperation,
                finalProject,
                Aggregation.sort(Sort.by("date").ascending())
        );

        return mongoTemplate.aggregate(aggregation, "transaction", ChargingDailyStat.class)
                .getMappedResults();
    }


    /**
     * 4. 하루 시간대 별 충전 횟수 / 충전량 조회
     */
    @Override
    public List<ChargingHourlyStat> getHourlyChargingStats(String stationId, LocalDate searchingDate) {

        Pair<Instant, Instant> inputDayRangeInKST = TimeUtils.getInputDayRangeInKST(searchingDate);

        MatchOperation match = getStationAndTimeRangeMatchOperation(inputDayRangeInKST, stationId);

        AggregationOperation projectToTime = getAggregationTimeFormatingOperation();

        GroupOperation groupOperation = Aggregation.group("hour")
                .sum("cost").as("totalRevenue")
                .sum("energyWh").as("totalEnergy")
                .count().as("count");

        ProjectionOperation finalProject = Aggregation.project()
                .and("_id").as("hour")
                .andInclude("totalRevenue", "totalEnergy", "count");

        Aggregation aggregation = Aggregation.newAggregation(
                match,
                projectToTime,
                groupOperation,
                finalProject,
                Aggregation.sort(Sort.by("hour").ascending())
        );

        return mongoTemplate.aggregate(aggregation, "transaction", ChargingHourlyStat.class)
                .getMappedResults();
    }


    /**
     * 5. 전체 충전 이력 조회 (검색조건)
     */
    @Override
    public Page<TransactionInfoResponse> findTransactionWithFilter(LocalDate from, LocalDate to, String stationId, TransactionStatus transactionStatus, Pageable pageable) {

        System.out.println("#####");

        Criteria criteria = getSearchingConditionCriteria(from, to, stationId, transactionStatus);

        MatchOperation match = Aggregation.match(criteria);

        SortOperation sort = Aggregation.sort(Sort.by(
                Sort.Order.desc("endTime"),
                Sort.Order.asc("_id")  // ✅ 유일한 필드 추가
        ));

        SkipOperation skip = Aggregation.skip(pageable.getOffset());
        LimitOperation limit = Aggregation.limit(pageable.getPageSize());

        AggregationOperation project = getAggregationDateFormatingOperationInFindTransactions();

        Aggregation aggregation = Aggregation.newAggregation(
                match,
                sort,
                skip,
                limit,
                project
        );

        List<TransactionInfoResponse> transaction = mongoTemplate.aggregate(aggregation, "transaction", TransactionInfoResponse.class)
                .getMappedResults();
        long total = mongoTemplate.count(new Query(criteria), Transaction.class);
        return new PageImpl<>(transaction, pageable, total);
    }

    /**
     * 6. 충전 프로파일 조회
     */
    @Override
    public Optional<ChargingProfileResponse> findChargingProfileById(String id) {

        Criteria criteria = Criteria.where("_id").is(id);
        Query query = new Query(criteria);
        query.fields()
                .include("energyWh")
                .include("cost")
                .include("startTime")
                .include("endTime")
                .include("startSchedule")
                .include("chargingProfileSnapshots");

        Transaction tx = mongoTemplate.findOne(query, Transaction.class);

        return Optional.ofNullable(tx)
                .map(ChargingProfileResponse::new);
    }

    //== 메서드 ==//
    private static MatchOperation getTimeRangeMatchOperation(Pair<Instant, Instant> timeRangeInKST) {
        Criteria criteria = Criteria.where("endTime")
                .gte(Date.from(timeRangeInKST.getFirst()))
                .lte(Date.from(timeRangeInKST.getSecond()))
                .and("transactionStatus").is("COMPLETED");

        return Aggregation.match(criteria);
    }

    private static MatchOperation getStationAndTimeRangeMatchOperation(Pair<Instant, Instant> timeRangeInKST, String stationId) {
        Criteria criteria = Criteria.where("endTime")
                .gte(Date.from(timeRangeInKST.getFirst()))
                .lte(Date.from(timeRangeInKST.getSecond()))
                .and("stationId").is(stationId)
                .and("transactionStatus").is("COMPLETED");

        return Aggregation.match(criteria);
    }

    private static AggregationOperation getAggregationDateFormatingOperation() {
        // $project: endTime -> "yyyy-MM-dd" 문자열로 포맷 (KST 기준)
        AggregationOperation projectToLocalDate = context -> new Document("$project",
                new Document("date", new Document("$dateToString",
                        new Document("format", "%Y-%m-%d")
                                .append("date", "$endTime")
                                .append("timezone", "Asia/Seoul")
                ))
                        .append("cost", 1)
                        .append("energyWh", 1)
        );
        return projectToLocalDate;
    }

    private static AggregationOperation getAggregationTimeFormatingOperation() {
        // $project: endTime -> "HH" 문자열로 포맷 (KST 기준)
        AggregationOperation projectToLocalDate = context -> new Document("$project",
                new Document("hour", new Document("$dateToString",
                        new Document("format", "%H")
                                .append("date", "$endTime")
                                .append("timezone", "Asia/Seoul")
                ))
                        .append("cost", 1)
                        .append("energyWh", 1)
        );
        return projectToLocalDate;
    }

    private static AggregationOperation getAggregationDateFormatingOperationInFindTransactions() {
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
                        .append("_id", 1)
                        .append("stationId", 1)
                        .append("evseId", 1)
                        .append("userId", 1)
                        .append("energyWh", 1)
                        .append("cost", 1)
                        .append("transactionStatus", 1)
        );
        return projectToLocalDate;
    }

    private static Criteria getSearchingConditionCriteria(LocalDate from, LocalDate to, String stationId,
                                                          TransactionStatus transactionStatus) {
        Criteria criteria = new Criteria();

        if (from != null && to != null) {
            Instant fromDate = TimeUtils.convertDateToUTC(from);
            Instant toDate = TimeUtils.convertDateToUTC(to.plusDays(1));
            criteria.and("endTime")
                    .gte(fromDate)
                    .lt(toDate);
        }

        if (stationId != null) {
            criteria.and("stationId").is(stationId);
        }

        if (transactionStatus != null) {
            criteria.and("transactionStatus").is(transactionStatus);
        }

        return criteria;
    }
}
