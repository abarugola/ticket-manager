package com.example.ticket_manager.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.example.ticket_manager.domain.ConcertStatus;
import com.example.ticket_manager.entity.Concert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ConcertRepository {

    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    public Concert save(Concert c) {
        dynamoDBMapper.save(c);
        return c;
    }

    public Concert findById(String id) {
        return dynamoDBMapper.load(Concert.class, id);
    }

    public List<Concert> findAll() {
        return dynamoDBMapper.scan(Concert.class, new DynamoDBScanExpression());
    }

    public List<Concert> findByAvailabilityStatus(ConcertStatus status) {
        Concert concertKey = new Concert();
        concertKey.setAvailabilityStatus(status);

        DynamoDBQueryExpression<Concert> queryExpression = new DynamoDBQueryExpression<Concert>()
                .withIndexName("availabilityStatus-date-index")
                .withHashKeyValues(concertKey)
                .withConsistentRead(false);

        return dynamoDBMapper.query(Concert.class, queryExpression);
    }

}
