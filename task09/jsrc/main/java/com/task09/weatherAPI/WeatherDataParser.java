package com.task09.weatherAPI;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class WeatherDataParser {

    public Map<String, Object> parseForecast(JsonNode weatherData) {
        Map<String, Object> forecast = new HashMap<>();
        forecast.put("elevation", weatherData.path("elevation").asDouble());
        forecast.put("generationtime_ms", weatherData.path("generationtime_ms").asDouble());
        forecast.put("latitude", weatherData.path("latitude").asDouble());
        forecast.put("longitude", weatherData.path("longitude").asDouble());
        forecast.put("timezone", weatherData.path("timezone").asText());
        forecast.put("timezone_abbreviation", weatherData.path("timezone_abbreviation").asText());
        forecast.put("utc_offset_seconds", weatherData.path("utc_offset_seconds").asInt());

        Map<String, Object> hourly = new HashMap<>();
        hourly.put("temperature_2m", parseArray(weatherData.path("hourly").path("temperature_2m")));
        hourly.put("time", parseArray(weatherData.path("hourly").path("time")));
        forecast.put("hourly", hourly);

        Map<String, Object> hourlyUnits = new HashMap<>();
        hourlyUnits.put("temperature_2m", weatherData.path("hourly_units").path("temperature_2m").asText());
        hourlyUnits.put("time", weatherData.path("hourly_units").path("time").asText());
        forecast.put("hourly_units", hourlyUnits);

        return forecast;
    }

    private Object parseArray(JsonNode arrayNode) {
        if (arrayNode.isArray()) {
            if (arrayNode.size() > 0 && arrayNode.get(0).isNumber()) {
                // Convert numbers to a list of doubles
                return StreamSupport.stream(arrayNode.spliterator(), false)
                        .map(JsonNode::asDouble)
                        .collect(Collectors.toList());
            } else {
                // Convert strings to a list of strings
                return StreamSupport.stream(arrayNode.spliterator(), false)
                        .map(JsonNode::asText)
                        .collect(Collectors.toList());
            }
        }
        return null;
    }
}
