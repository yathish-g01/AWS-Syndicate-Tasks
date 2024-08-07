package com.task09.weatherDTO;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;

public class Forecast {

    private Double elevation;
    private Double generationtimeMs;
    private Hourly hourly;
    private HourlyUnits hourlyUnits;
    private Double latitude;
    private Double longitude;
    private String timezone;
    private String timezoneAbbreviation;
    private Integer utcOffsetSeconds;

    @DynamoDBAttribute(attributeName = "elevation")
    public Double getElevation() {
        return elevation;
    }

    public void setElevation(Double elevation) {
        this.elevation = elevation;
    }

    @DynamoDBAttribute(attributeName = "generationtime_ms")
    public Double getGenerationtimeMs() {
        return generationtimeMs;
    }

    public void setGenerationtimeMs(Double generationtimeMs) {
        this.generationtimeMs = generationtimeMs;
    }

    @DynamoDBAttribute(attributeName = "hourly")
    public Hourly getHourly() {
        return hourly;
    }

    public void setHourly(Hourly hourly) {
        this.hourly = hourly;
    }

    @DynamoDBAttribute(attributeName = "hourly_units")
    public HourlyUnits getHourlyUnits() {
        return hourlyUnits;
    }

    public void setHourlyUnits(HourlyUnits hourlyUnits) {
        this.hourlyUnits = hourlyUnits;
    }

    @DynamoDBAttribute(attributeName = "latitude")
    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    @DynamoDBAttribute(attributeName = "longitude")
    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @DynamoDBAttribute(attributeName = "timezone")
    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    @DynamoDBAttribute(attributeName = "timezone_abbreviation")
    public String getTimezoneAbbreviation() {
        return timezoneAbbreviation;
    }

    public void setTimezoneAbbreviation(String timezoneAbbreviation) {
        this.timezoneAbbreviation = timezoneAbbreviation;
    }

    @DynamoDBAttribute(attributeName = "utc_offset_seconds")
    public Integer getUtcOffsetSeconds() {
        return utcOffsetSeconds;
    }

    public void setUtcOffsetSeconds(Integer utcOffsetSeconds) {
        this.utcOffsetSeconds = utcOffsetSeconds;
    }
}
