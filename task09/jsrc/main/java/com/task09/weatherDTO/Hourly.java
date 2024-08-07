package com.task09.weatherDTO;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;

import java.util.List;

public class Hourly {

    private List<Double> temperature2m;
    private List<String> time;

    @DynamoDBAttribute(attributeName = "temperature_2m")
    public List<Double> getTemperature2m() {
        return temperature2m;
    }

    public void setTemperature2m(List<Double> temperature2m) {
        this.temperature2m = temperature2m;
    }

    @DynamoDBAttribute(attributeName = "time")
    public List<String> getTime() {
        return time;
    }

    public void setTime(List<String> time) {
        this.time = time;
    }
}
