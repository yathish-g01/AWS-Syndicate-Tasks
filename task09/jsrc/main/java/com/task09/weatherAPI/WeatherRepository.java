package com.task09.weatherAPI;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WeatherRepository {

    private final DynamoDB dynamoDb;
    private final WeatherDataParser weatherDataParser;
    private static final String DYNAMODB_TABLE_NAME = "Weather";

    public WeatherRepository(DynamoDB dynamoDb) {
        this.dynamoDb = dynamoDb;
        this.weatherDataParser = new WeatherDataParser();
    }

    public void saveWeatherData(String id, String weatherDataJson) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode weatherData = mapper.readTree(weatherDataJson);
        Table table = dynamoDb.getTable(DYNAMODB_TABLE_NAME);

        Item item = new Item()
                .withPrimaryKey("id", id)
                .withMap("forecast", weatherDataParser.parseForecast(weatherData));

        table.putItem(item);
    }
}
