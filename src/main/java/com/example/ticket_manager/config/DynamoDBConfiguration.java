package com.example.ticket_manager.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.example.ticket_manager.entity.Concert;
import com.example.ticket_manager.entity.Seat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DynamoDBConfiguration {

    @Value("${aws.dynamodb.endpoint}")
    private String endpoint;

    @Value("${aws.dynamodb.region}")
    private String region;

    @Value("${aws.dynamodb.access-key}")
    private String accessKey;

    @Value("${aws.dynamodb.secret-key}")
    private String secretKey;

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {

        return AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(
                                endpoint,
                                region
                        )
                )
                .withCredentials(
                        new AWSStaticCredentialsProvider(
                                new BasicAWSCredentials(
                                        accessKey,
                                        secretKey
                                )
                        )
                )
                .build();
    }

    @Bean
    public DynamoDBMapper dynamoDBMapper(
            AmazonDynamoDB amazonDynamoDB) {

        return new DynamoDBMapper(amazonDynamoDB);
    }

    @Bean
    public ApplicationRunner initializeDynamoDBTables(AmazonDynamoDB amazonDynamoDB, DynamoDBMapper dynamoDBMapper) {
        return args -> {
            List<Class<?>> entityClasses = List.of(Concert.class, Seat.class);

            for (Class<?> entityClass : entityClasses) {
                createTableIfNotExists(amazonDynamoDB, dynamoDBMapper, entityClass);
            }
        };
    }

    private void createTableIfNotExists(AmazonDynamoDB amazonDynamoDB, DynamoDBMapper dynamoDBMapper, Class<?> entityClass) {
        CreateTableRequest tableRequest = dynamoDBMapper.generateCreateTableRequest(entityClass);

        tableRequest.setProvisionedThroughput(new ProvisionedThroughput(5L, 5L));

        if (tableRequest.getGlobalSecondaryIndexes() != null) {
            tableRequest.getGlobalSecondaryIndexes().forEach(gsi -> {
                gsi.setProvisionedThroughput(new ProvisionedThroughput(5L, 5L));
                gsi.getProjection().setProjectionType("ALL");
            });
        }

        TableUtils.createTableIfNotExists(amazonDynamoDB, tableRequest);
    }

}
