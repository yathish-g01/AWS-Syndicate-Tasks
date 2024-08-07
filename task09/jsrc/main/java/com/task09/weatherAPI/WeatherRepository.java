package com.task09.weatherAPI;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task09.weatherDTO.DynamoDBWeatherData;

public class WeatherRepository {

    private final DynamoDBMapper dynamoDbMapper;
    private final WeatherDataParser weatherDataParser;

    public WeatherRepository(DynamoDBMapper dynamoDbMapper) {
        this.dynamoDbMapper = dynamoDbMapper;
        this.weatherDataParser = new WeatherDataParser();
    }

    public void saveWeatherData(String id, String weatherDataJson) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode weatherData = mapper.readTree(weatherDataJson);

        // Parse the JSON into DynamoDBWeatherData object
        DynamoDBWeatherData weatherDataItem = weatherDataParser.parseWeatherData(weatherData);
        weatherDataItem.setId(id);

        // Save the item to DynamoDB
        dynamoDbMapper.save(weatherDataItem, DynamoDBMapperConfig.DEFAULT);
    }
}
