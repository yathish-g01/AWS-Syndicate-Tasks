package com.task02;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;

import java.util.HashMap;
import java.util.Map;

@LambdaHandler(lambdaName = "hello_world",
	roleName = "hello_world-role",
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
public class LambdaFunctionHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	@Override
	public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
		context.getLogger().log("Received event: " + event);

		// Check if the request is for the /hello endpoint with the GET method
		if ("/hello".equals(event.getRawPath()) && "GET".equalsIgnoreCase(event.getRequestContext().getHttp().getMethod())) {
			return APIGatewayV2HTTPResponse.builder()
					.withStatusCode(200)
					.withHeaders(Collections.singletonMap("Content-Type", "application/json"))
					.withBody("{\"statusCode\": 200, \"message\": \"Hello from Lambda\"}")
					.build();
		} else {
			String path = event.getRawPath();
			String method = event.getRequestContext().getHttp().getMethod();
			String errorMessage = String.format("{\"statusCode\": 400, \"message\": \"Bad request syntax or unsupported method. Request path: %s. HTTP method: %s\"}", path, method);

			return APIGatewayV2HTTPResponse.builder()
					.withStatusCode(400)
					.withHeaders(Collections.singletonMap("Content-Type", "application/json"))
					.withBody(errorMessage)
					.build();
		}
	}
}