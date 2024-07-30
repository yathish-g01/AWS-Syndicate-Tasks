package com.task05;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(lambdaName = "api_handler",
		roleName = "api_handler-role",
		isPublishVersion = false,
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
@DependsOn(resourceType = ResourceType.DYNAMODB_TABLE, name = "Events")
@EnvironmentVariables(value = {
		@EnvironmentVariable(key = "region", value = "eu-central-1"),
		@EnvironmentVariable(key = "table", value = "Events"),})
public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private final AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.defaultClient();
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
		Map<String, String> content = new HashMap<>();
		int principalId = 0;
		String createdAt = "2023-10-20T08:51:33.123Z";
		String id = UUID.randomUUID().toString();

		// Convert content to a Map<String, AttributeValue>
		Map<String, AttributeValue> contentAttributeValueMap = new HashMap<>();
		for (Map.Entry<String, String> entry : content.entrySet()) {
			contentAttributeValueMap.put(entry.getKey(), new AttributeValue(entry.getValue()));
		}

		// Save to DynamoDB
		Map<String, AttributeValue> event = new HashMap<>();
		event.put("id", new AttributeValue(id));
		event.put("principalId", new AttributeValue().withN(String.valueOf(principalId)));
		event.put("createdAt", new AttributeValue(createdAt));
		event.put("body", new AttributeValue().withM(contentAttributeValueMap));

		PutItemRequest putItemRequest = new PutItemRequest().withTableName("Events").withItem(event);
		dynamoDB.putItem(putItemRequest);

		String eventJson = null;
		try {
			eventJson = objectMapper.writeValueAsString(event);
		} catch (JsonProcessingException e) {
			context.getLogger().log("Error converting event to JSON: " + e.getMessage());
		}

		// Create response
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		response.setStatusCode(201);
		response.setBody(eventJson);

		return response;
	}
}
