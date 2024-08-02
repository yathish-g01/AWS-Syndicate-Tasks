package com.task05;
//
//import com.amazonaws.regions.Regions;
//import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
//import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
//import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
//import com.amazonaws.services.lambda.runtime.Context;
//import com.amazonaws.services.lambda.runtime.LambdaLogger;
//import com.amazonaws.services.lambda.runtime.RequestHandler;
//import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
//import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
//import com.syndicate.deployment.annotations.lambda.LambdaHandler;
//import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
//import com.syndicate.deployment.annotations.resources.DependsOn;
//import com.syndicate.deployment.model.ResourceType;
//import com.syndicate.deployment.model.RetentionSetting;
//import com.syndicate.deployment.model.lambda.url.AuthType;
//import com.syndicate.deployment.model.lambda.url.InvokeMode;
//import com.task05.dto.Event;
//import com.task05.dto.Request;
//import com.task05.dto.Response;
//
//import java.time.LocalDateTime;
//import java.util.Map;
//import java.util.UUID;
//
//
//@LambdaHandler(lambdaName = "api_handler",
//		roleName = "api_handler-role",
//		isPublishVersion = false,
//		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
//)
//@LambdaUrlConfig(
//		authType = AuthType.NONE,
//		invokeMode = InvokeMode.BUFFERED
//)
//@EnvironmentVariables(value = {
//		@EnvironmentVariable(key = "region", value = "${region}"),
//		@EnvironmentVariable(key = "table", value = "${target_table}")})
//@DependsOn(name = "Events", resourceType = ResourceType.DYNAMODB_TABLE)
//public class ApiHandler implements RequestHandler<Request, Response> {
//
//	private Gson gson;
//	private ObjectContentMapper mapper;
//	private DynamoDBMapper dynamoDB;
//	private LambdaLogger log;
//
//	private final int STATUS_OK = 201;
//
//	private void init(Context context){
//		log = context.getLogger();
//		gson = new GsonBuilder().create();
//		mapper = new ObjectContentMapper(gson);
//		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).build();
//		dynamoDB = new DynamoDBMapper(client);
//	}
//
//
//	@Override
//	public Response handleRequest(Request request, Context context) {
//		init(context);
//		log.log(gson.toJson(request));
//		return new Response(STATUS_OK, addDataAndResponse(request));
//	}
//
//	private Event addDataAndResponse(Request request){
//		String id = UUID.randomUUID().toString();
//		LocalDateTime createdAt = LocalDateTime.now();
//		Event event = new Event();
//		event.setId(id);
//		event.setCreatedAt(createdAt.toString());
//		event.setPrincipalId(request.getPrincipalId());
//		try {
//			event.setBody(mapper.objectToContent(request.getContent()));
//		} catch (Exception e) {
//			log.log("WARNING: Failed to convert request body to event content");
//			event.setBody(
//					Map.of("error",
//							"wrong content provided" +
//									"but was" + gson.toJson(request.getContent())
//					)
//			);
//		}
//		log.log("Event created: " + gson.toJson(event));
//		return saveEvent(event);
//	}
//
//	private Event saveEvent(Event event){
//		DynamoDBEvent item = new DynamoDBEvent();
//		item.setId(event.getId());
//		item.setPrincipalId(event.getPrincipalId());
//		item.setCreatedAt(event.getCreatedAt());
//		item.setBody(event.getBody());
//		dynamoDB.save(item);
//		log.log("Item saved as: " + gson.toJson(item));
//		return event;
//	}
//}

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
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
public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private final DynamoDbClient dynamoDb = DynamoDbClient.builder().build();
	private final String tableName = "cmtr-6c6b70bd-Events";
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
		// Parse the request body
		Map<String, String> content = new HashMap<>(); // Replace with actual parsing logic
		int principalId = 0; // Replace with actual parsing logic

		// Generate a unique event ID and current timestamp
		String eventId = UUID.randomUUID().toString();
		String createdAt = Instant.now().toString();

		// Create the event item
		Map<String, AttributeValue> item = new HashMap<>();
		item.put("id", AttributeValue.builder().s(eventId).build());
		item.put("principalId", AttributeValue.builder().n(Integer.toString(principalId)).build());
		item.put("createdAt", AttributeValue.builder().s(createdAt).build());
		item.put("body", AttributeValue.builder().m(mapStringToAttributeValue(content)).build());

		// Save the event to DynamoDB
		PutItemRequest putItemRequest = PutItemRequest.builder()
				.tableName(tableName)
				.item(item)
				.build();
		dynamoDb.putItem(putItemRequest);

		// Create the response
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		response.setStatusCode(201);
		response.setBody(createResponseBody(eventId, principalId, createdAt, content));

		return response;
	}

	private String createResponseBody(String eventId, int principalId, String createdAt, Map<String, String> content) {
		Map<String, Object> responseBody = new HashMap<>();
		Map<String, Object> event = new HashMap<>();
		event.put("id", eventId);
		event.put("principalId", principalId);
		event.put("createdAt", createdAt);
		event.put("body", content);

		responseBody.put("statusCode", 201);
		responseBody.put("event", event);

		try {
			return objectMapper.writeValueAsString(responseBody);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Error processing JSON", e);
		}
	}

	private Map<String, AttributeValue> mapStringToAttributeValue(Map<String, String> map) {
		Map<String, AttributeValue> attributeValueMap = new HashMap<>();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			attributeValueMap.put(entry.getKey(), AttributeValue.builder().s(entry.getValue()).build());
		}
		return attributeValueMap;
	}
}