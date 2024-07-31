package com.task05;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(
		lambdaName = "api_handler",
		roleName = "api_handler-role",
		isPublishVersion = false,
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)

@DependsOn(name="Events", resourceType = ResourceType.DYNAMODB_TABLE)

@EnvironmentVariables(value = {
		@EnvironmentVariable(key = "region", value = "eu-central-1"),
		@EnvironmentVariable(key = "table", value = "Events"),})
public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private final DynamoDbClient dynamoDbClient = DynamoDbClient.builder().build();
	private final String tableName = "Events";
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
		Map<String, Object> requestBody;
		try {
			requestBody = objectMapper.readValue(request.getBody(), new TypeReference<Map<String, Object>>() {});
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody("Invalid request body");
		}

		Integer principalId = (Integer) requestBody.get("principalId");
		Map<String, String> content;
		try {
			content = objectMapper.convertValue(requestBody.get("content"), new TypeReference<Map<String, String>>() {});
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody("Invalid content format");
		}

		String eventId = UUID.randomUUID().toString();
		String createdAt = Instant.now().toString();

		Map<String, AttributeValue> item = new HashMap<>();
		item.put("id", AttributeValue.builder().s(eventId).build());
		item.put("principalId", AttributeValue.builder().n(principalId.toString()).build());
		item.put("createdAt", AttributeValue.builder().s(createdAt).build());
		item.put("body", AttributeValue.builder().m(mapStringToAttributeValue(content)).build());

		PutItemRequest putItemRequest = PutItemRequest.builder()
				.tableName(tableName)
				.item(item)
				.build();

		dynamoDbClient.putItem(putItemRequest);

		Map<String, Object> event = new HashMap<>();
		event.put("id", eventId);
		event.put("principalId", principalId);
		event.put("createdAt", createdAt);
		event.put("body", content);

		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		response.setStatusCode(201);
		try {
			response.setBody(objectMapper.writeValueAsString(event));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody("Error processing response");
		}
		return response;
	}

	private Map<String, AttributeValue> mapStringToAttributeValue(Map<String, String> map) {
		Map<String, AttributeValue> attributeValueMap = new HashMap<>();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			attributeValueMap.put(entry.getKey(), AttributeValue.builder().s(entry.getValue()).build());
		}
		return attributeValueMap;
	}
}