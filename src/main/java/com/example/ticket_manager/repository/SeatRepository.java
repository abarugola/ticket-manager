package com.example.ticket_manager.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.example.ticket_manager.domain.SeatStatus;
import com.example.ticket_manager.entity.Seat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SeatRepository {

    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    public Seat save(Seat seat) {
        dynamoDBMapper.save(seat);
        return seat;
    }

    public List<Seat> saveAll(List<Seat> seats) {
        dynamoDBMapper.batchSave(seats);
        return seats;
    }

    public Seat findByConcertIdAndSeatId(String concertId, String seatId) {
        return dynamoDBMapper.load(Seat.class, concertId, seatId);
    }

    public List<Seat> findAllByConcertId(String concertId) {
        Seat keyObject = Seat.builder()
                .concertId(concertId)
                .build();

        DynamoDBQueryExpression<Seat> queryExpression = new DynamoDBQueryExpression<Seat>()
                .withHashKeyValues(keyObject);

        return dynamoDBMapper.query(Seat.class, queryExpression);
    }

    public List<Seat> findByConcertIdAndStatus(String concertId, SeatStatus status) {
        Seat keyObject = Seat.builder()
                .concertId(concertId)
                .build();

        Condition rangeKeyCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ)
                .withAttributeValueList(new AttributeValue().withS(status.name()));

        DynamoDBQueryExpression<Seat> queryExpression = new DynamoDBQueryExpression<Seat>()
                .withIndexName("concertStatus-index")
                .withHashKeyValues(keyObject)
                .withRangeKeyCondition("status", rangeKeyCondition)
                .withConsistentRead(false);

        return dynamoDBMapper.query(Seat.class, queryExpression);
    }

    public List<Seat> findAvailableSeats(String concertId) {
        return findByConcertIdAndStatus(concertId, SeatStatus.AVAILABLE);
    }

    public void saveSeatIfAvailable(Seat seat) {
        DynamoDBSaveExpression saveExpression = new DynamoDBSaveExpression();

        saveExpression.withExpectedEntry(
                "status",
                new ExpectedAttributeValue(new AttributeValue().withS(SeatStatus.AVAILABLE.name()))
        );

        dynamoDBMapper.save(seat, saveExpression);
    }
}