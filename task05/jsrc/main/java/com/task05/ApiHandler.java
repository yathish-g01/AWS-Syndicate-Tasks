package com.task05;


import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
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
import java.util.LinkedHashMap;

import java.time.LocalDateTime;
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
@EnvironmentVariables(value = {
		@EnvironmentVariable(key = "region", value = "${region}"),
		@EnvironmentVariable(key = "table", value = "${target_table}")})
@DependsOn(name = "Events", resourceType = ResourceType.DYNAMODB_TABLE)
public class ApiHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	private static final int STATUS_CREATED = 201;
	private static final String TABLE_NAME = "cmtr-6c6b70bd-Events";
	private final Map<String, String> responseHeaders = Map.of("Content-Type", "application/json");

	@Override
	public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent request, Context context) {
		try {
			System.out.println("Table name: " + TABLE_NAME);
			return handlePost(request);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	private APIGatewayV2HTTPResponse handlePost(APIGatewayV2HTTPEvent request) throws JsonProcessingException {
		Map<String, Object> result = saveToDynamoDb(request);
		APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
		response.setStatusCode(STATUS_CREATED);
		response.setHeaders(responseHeaders);
		response.setBody(new ObjectMapper().writeValueAsString(result));
		return response;
	}

	private Map<String, Object> saveToDynamoDb(APIGatewayV2HTTPEvent requestEvent) throws JsonProcessingException {
		final AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder.standard().build();
		ObjectMapper objectMapper = new ObjectMapper();
		System.out.println("Request body: " + requestEvent.getBody());
		Map<String, Object> inputJson = objectMapper.readValue(requestEvent.getBody(), new TypeReference<Map<String, Object>>() {});

		String id = UUID.randomUUID().toString();
		String principalId = inputJson.get("principalId").toString();
		System.out.println("principalId: " + principalId);
		String createdAt = getISOFormattedDateTime();
		Object body = inputJson.get("content");
		if (body == null) {
			throw new IllegalArgumentException("argument \"content\" is null");
		}
		System.out.println("content: " + body);

		Map<String, AttributeValue> itemValues = new LinkedHashMap<>();
		itemValues.put("id", new AttributeValue().withS(id));
		itemValues.put("principalId", new AttributeValue().withS(principalId));
		itemValues.put("createdAt", new AttributeValue().withS(createdAt));
		itemValues.put("body", new AttributeValue().withS(objectMapper.writeValueAsString(body))); // Serialize the body object to JSON string

		System.out.println(itemValues);

		PutItemRequest putItemRequest = new PutItemRequest().withTableName(TABLE_NAME).withItem(itemValues);
		amazonDynamoDB.putItem(putItemRequest);

		// Return the result as a map
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("id", id);
		result.put("principalId", principalId);
		result.put("createdAt", createdAt);
		result.put("body", body);
		return result;
	}

	private String getISOFormattedDateTime() {
		return java.time.Instant.now().toString();
	}
}