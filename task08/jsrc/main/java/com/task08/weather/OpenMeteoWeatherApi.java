package com.task08.weather;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class OpenMeteoWeatherApi {

    private static final String BASE_URL = "https://api.open-meteo.com/v1/forecast";

    public String getWeatherForecast(double latitude, double longitude) throws Exception {
        String url = String.format("%s?latitude=%.4f&longitude=%.4f&hourly=temperature_2m,relative_humidity_2m,wind_speed_10m", BASE_URL, latitude, longitude);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        } else {
            throw new Exception("Failed to retrieve weather data");
        }
    }
}
