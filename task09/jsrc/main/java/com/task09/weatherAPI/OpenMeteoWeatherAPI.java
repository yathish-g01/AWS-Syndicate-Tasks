package com.task09.weatherAPI;

import software.amazon.awssdk.http.HttpExecuteRequest;
import software.amazon.awssdk.http.HttpExecuteResponse;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.utils.IoUtils;
import java.io.InputStream;
import java.net.URI;

public class OpenMeteoWeatherAPI {

    private static final String WEATHER_API_URL = "https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&current=temperature_2m,wind_speed_10m&hourly=temperature_2m,relative_humidity_2m,wind_speed_10m";

    public String fetchWeatherData() throws Exception {
        UrlConnectionHttpClient httpClient = (UrlConnectionHttpClient) UrlConnectionHttpClient.create();
        SdkHttpFullRequest request = SdkHttpFullRequest.builder()
                .method(SdkHttpMethod.GET)
                .uri(URI.create(WEATHER_API_URL))
                .build();

        HttpExecuteRequest executeRequest = HttpExecuteRequest.builder().request(request).build();
        HttpExecuteResponse response = httpClient.prepareRequest(executeRequest).call();

        if (response.httpResponse().isSuccessful()) {
            try (InputStream responseBodyStream = response.responseBody().get()) {
                return IoUtils.toUtf8String(responseBodyStream);
            }
        } else {
            throw new Exception("Failed to fetch weather data");
        }
    }
}
