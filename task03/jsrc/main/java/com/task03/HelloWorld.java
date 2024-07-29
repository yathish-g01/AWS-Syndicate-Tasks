package com.task03;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import java.util.HashMap;
import java.util.Map;

@LambdaHandler(lambdaName = "hello_world",
	roleName = "hello_world-role",
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
public class HelloWorld implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
		context.getLogger().log("Received event: " + event);

		// Create headers
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", "application/json");

		// Check if the request is for the /hello resource with the GET method
		if ("/hello".equals(event.getPath()) && "GET".equalsIgnoreCase(event.getHttpMethod())) {
			// Return the predefined message
			return new APIGatewayProxyResponseEvent()
					.withStatusCode(200)
					.withHeaders(headers)
					.withBody("{\"statusCode\": 200, \"message\": \"Hello from Lambda\"}");
		} else {
			// Return a 400 Bad Request error for any other endpoint
			return new APIGatewayProxyResponseEvent()
					.withStatusCode(400)
					.withHeaders(headers)
					.withBody("{\"statusCode\": 400, \"message\": \"Bad request syntax or unsupported method.\"}");
		}
	}
}