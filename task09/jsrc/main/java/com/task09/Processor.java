package com.task09;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.AWSXRayRecorder;
import com.amazonaws.xray.entities.Subsegment;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.TracingMode;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import com.task09.weatherAPI.OpenMeteoWeatherAPI;
import com.task09.weatherAPI.WeatherRepository;

import java.util.UUID;

@LambdaHandler(lambdaName = "processor",
        roleName = "processor-role",
        isPublishVersion = false,
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED,
        tracingMode = TracingMode.Active
)
@LambdaUrlConfig(
        authType = AuthType.NONE,
        invokeMode = InvokeMode.BUFFERED
)
@EnvironmentVariables(value =
        {
                @EnvironmentVariable(key = "target_table", value = "${target_table}"),
                @EnvironmentVariable(key = "region", value = "${region}")
        })
public class Processor implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        DynamoDBMapper dynamoDbMapper = new DynamoDBMapper(client);
        WeatherRepository weatherRepository = new WeatherRepository(dynamoDbMapper);
        AWSXRayRecorder recorder = AWSXRay.getGlobalRecorder();
        Subsegment subsegment = recorder.beginSubsegment("ProcessWeatherData");

        try {
            // Fetch weather data
            OpenMeteoWeatherAPI weatherApi = new OpenMeteoWeatherAPI();
            String id = UUID.randomUUID().toString();
            String weatherData = weatherApi.fetchWeatherData();

            // Save to DynamoDB
            weatherRepository.saveWeatherData(id, weatherData);

            // Return response
            return createResponse(200, "Weather data saved with ID: " + id);
        } catch (Exception e) {
            subsegment.addException(e);
            return createResponse(500, "Error: " + e.getMessage());
        } finally {
            recorder.endSubsegment();
        }
    }

    private APIGatewayProxyResponseEvent createResponse(int statusCode, String body) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(statusCode);
        response.setBody(body);
        return response;
    }
}