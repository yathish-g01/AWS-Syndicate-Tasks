//package com.task08;
//
//import com.amazonaws.services.lambda.runtime.Context;
//import com.amazonaws.services.lambda.runtime.RequestHandler;
//import com.syndicate.deployment.annotations.lambda.LambdaHandler;
//import com.syndicate.deployment.annotations.lambda.LambdaLayer;
//import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
//import com.syndicate.deployment.model.Architecture;
//import com.syndicate.deployment.model.ArtifactExtension;
//import com.syndicate.deployment.model.DeploymentRuntime;
//import com.syndicate.deployment.model.RetentionSetting;
//import com.syndicate.deployment.model.lambda.url.AuthType;
//import com.syndicate.deployment.model.lambda.url.InvokeMode;
//import com.task08.weather.OpenMeteoWeatherApi;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@LambdaHandler(lambdaName = "api_handler",
//        roleName = "api_handler-role",
//        layers = {"open-meteo-weather-api"},
//        isPublishVersion = false,
//        logsExpiration =  RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
//)
//@LambdaLayer(layerName = "open-meteo-weather-api",
//        libraries = {"layer/java/lib/weather-lambda-java-1.0-SNAPSHOT.jar"},
//        runtime = DeploymentRuntime.JAVA11,
//        architectures = {Architecture.ARM64},
//        artifactExtension = ArtifactExtension.ZIP
//)
//@LambdaUrlConfig(
//        authType = AuthType.NONE,
//        invokeMode = InvokeMode.BUFFERED
//)
//public class ApiHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {
//
//    @Override
//    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
//        Map<String, Object> response = new HashMap<>();
//        OpenMeteoWeatherApi client = new OpenMeteoWeatherApi();
//
//        try {
//            String weatherData = client.getWeatherForecast(50.4375, 30.5); // Example coordinates
//            response.put("statusCode", 200);
//            response.put("body", weatherData);
//        } catch (Exception e) {
//            response.put("statusCode", 500);
//            response.put("body", "Error: " + e.getMessage());
//        }
//
//        return response;
//    }
//}

package com.task08;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.Architecture;
import com.syndicate.deployment.model.ArtifactExtension;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import com.task08.weather.OpenMeteoWeatherApi;

import java.util.HashMap;
import java.util.Map;

@LambdaHandler(lambdaName = "api_handler",
        roleName = "api_handler-role",
        layers = {"open-meteo-weather-api"},
        isPublishVersion = false,
        logsExpiration =  RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaLayer(layerName = "open-meteo-weather-api",
        libraries = {"layer/java/lib/weather-lambda-java-1.0-SNAPSHOT.jar"},
        runtime = DeploymentRuntime.JAVA11,
        architectures = {Architecture.ARM64},
        artifactExtension = ArtifactExtension.ZIP
)
@LambdaUrlConfig(
        authType = AuthType.NONE,
        invokeMode = InvokeMode.BUFFERED
)
public class ApiHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        Map<String, Object> response = new HashMap<>();

        // Ensure that the SDK from the layer is being used
        try {
            OpenMeteoWeatherApi client = new OpenMeteoWeatherApi(); // Utilizing the SDK provided by the layer
            String weatherData = client.getWeatherForecast(52.52, 13.41); // Example coordinates (Berlin)

            response.put("statusCode", 200);
            response.put("body", weatherData);
        } catch (NoClassDefFoundError e) {
            // This error might occur if the layer is not correctly attached
            response.put("statusCode", 500);
            response.put("body", "Error: SDK not found. Make sure the Lambda layer is correctly configured. Details: " + e.getMessage());
        } catch (Exception e) {
            response.put("statusCode", 500);
            response.put("body", "Error: " + e.getMessage());
        }

        return response;
    }
}

