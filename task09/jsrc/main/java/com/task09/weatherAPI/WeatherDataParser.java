package com.task09.weatherAPI;

import com.fasterxml.jackson.databind.JsonNode;
import com.task09.weatherDTO.DynamoDBWeatherData;
import com.task09.weatherDTO.Forecast;
import com.task09.weatherDTO.Hourly;
import com.task09.weatherDTO.HourlyUnits;

import java.util.ArrayList;
import java.util.List;

public class WeatherDataParser {

    public DynamoDBWeatherData parseWeatherData(JsonNode weatherData) {
        DynamoDBWeatherData weatherDataItem = new DynamoDBWeatherData();
        weatherDataItem.setId(weatherData.get("id").asText());

        Forecast forecast = new Forecast();
        forecast.setElevation(weatherData.get("forecast").get("elevation").asDouble());
        forecast.setGenerationtimeMs(weatherData.get("forecast").get("generationtime_ms").asDouble());
        forecast.setLatitude(weatherData.get("forecast").get("latitude").asDouble());
        forecast.setLongitude(weatherData.get("forecast").get("longitude").asDouble());
        forecast.setTimezone(weatherData.get("forecast").get("timezone").asText());
        forecast.setTimezoneAbbreviation(weatherData.get("forecast").get("timezone_abbreviation").asText());
        forecast.setUtcOffsetSeconds(weatherData.get("forecast").get("utc_offset_seconds").asInt());

        // Parse Hourly
        JsonNode hourlyNode = weatherData.get("forecast").get("hourly");
        Hourly hourly = new Hourly();
        hourly.setTemperature2m(parseDoubleList(hourlyNode.get("temperature_2m")));
        hourly.setTime(parseStringList(hourlyNode.get("time")));
        forecast.setHourly(hourly);

        // Parse HourlyUnits
        JsonNode hourlyUnitsNode = weatherData.get("forecast").get("hourly_units");
        HourlyUnits hourlyUnits = new HourlyUnits();
        hourlyUnits.setTemperature2m(hourlyUnitsNode.get("temperature_2m").asText());
        hourlyUnits.setTime(hourlyUnitsNode.get("time").asText());
        forecast.setHourlyUnits(hourlyUnits);

        weatherDataItem.setForecast(forecast);

        return weatherDataItem;
    }

    private List<Double> parseDoubleList(JsonNode node) {
        List<Double> list = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode element : node) {
                list.add(element.asDouble());
            }
        }
        return list;
    }

    private List<String> parseStringList(JsonNode node) {
        List<String> list = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode element : node) {
                list.add(element.asText());
            }
        }
        return list;
    }
}
