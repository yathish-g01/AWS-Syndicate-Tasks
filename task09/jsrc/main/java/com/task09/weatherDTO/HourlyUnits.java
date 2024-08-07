package com.task09.weatherDTO;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;

public class HourlyUnits {

    private String temperature2m;
    private String time;

    @DynamoDBAttribute(attributeName = "temperature_2m")
    public String getTemperature2m() {
        return temperature2m;
    }

    public void setTemperature2m(String temperature2m) {
        this.temperature2m = temperature2m;
    }

    @DynamoDBAttribute(attributeName = "time")
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
