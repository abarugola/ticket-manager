package com.example.ticket_manager.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.example.ticket_manager.domain.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@DynamoDBTable(tableName = "seat")
public class Seat {
    @DynamoDBHashKey
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "concertStatus-index")
    private String concertId;

    @DynamoDBRangeKey
    private String seatId;

    @DynamoDBAttribute
    private String row;

    @DynamoDBAttribute
    private int number;

    @DynamoDBAttribute
    private long priceCents;

    @DynamoDBIndexRangeKey(globalSecondaryIndexName = "concertStatus-index")
    @DynamoDBTypeConvertedEnum
    private SeatStatus status;

    @DynamoDBAttribute
    private String heldBy;

    @DynamoDBAttribute
    private Date holdExpiresAt;
}
