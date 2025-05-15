package com.chargeset.chargeset_server.repository.evse;

import com.chargeset.chargeset_server.dto.EvseIdOnly;
import com.chargeset.chargeset_server.dto.charging_station.EvseStatusCount;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@RequiredArgsConstructor
public class EvseCustomRepositoryImpl implements EvseCustomRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<EvseStatusCount> countEvseStatus() {

        GroupOperation groupOperation = Aggregation.group("evseStatus").count().as("count");

        ProjectionOperation projectionOperation = Aggregation.project()
                .and("_id").as("status")
                .andInclude("count");

        Aggregation aggregation = Aggregation.newAggregation(groupOperation, projectionOperation);

        return mongoTemplate.aggregate(aggregation, "evse", EvseStatusCount.class).getMappedResults();
    }

    @Override
    public List<EvseStatusCount> countEvseStatusByStationId(String stationId) {

        Criteria criteria = Criteria.where("stationId").is(stationId);

        MatchOperation match = Aggregation.match(criteria);

        GroupOperation groupOperation = Aggregation.group("evseStatus").count().as("count");

        ProjectionOperation projectionOperation = Aggregation.project()
                .and("_id").as("status")
                .andInclude("count");

        Aggregation aggregation = Aggregation.newAggregation(match, groupOperation, projectionOperation);

        return mongoTemplate.aggregate(aggregation, "evse", EvseStatusCount.class).getMappedResults();
    }


    @Override
    public List<EvseIdOnly> findAvailableEvseIds(List<String> occupiedEvseIds, String stationId) {

        Criteria criteria = Criteria.where("stationId").is(stationId);

        if (occupiedEvseIds != null && !occupiedEvseIds.isEmpty()) {
            criteria = criteria.and("evseId").nin(occupiedEvseIds);
        }

        MatchOperation match = Aggregation.match(criteria);

        ProjectionOperation project = Aggregation.project("evseId");

        Aggregation aggregation = Aggregation.newAggregation(match, project);

        return mongoTemplate.aggregate(aggregation, "evse", EvseIdOnly.class).getMappedResults();
    }
}
