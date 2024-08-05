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


package com.task05;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import com.task05.dto.Event;
import com.task05.dto.Request;
import com.task05.dto.Response;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(lambdaName = "api_handler",
		roleName = "api_handler-role",
		isPublishVersion = false,
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@EnvironmentVariables(value = {
		@EnvironmentVariable(key = "target_table", value = "${target_table}")
})
public class ApiHandler implements RequestHandler<Request, Response> {

	AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
	private DynamoDB dynamoDb = new DynamoDB(client);
	private String DYNAMODB_TABLE_NAME = System.getenv("target_table");

	@Override
	public Response handleRequest(Request event1, Context context) {

		int principalId = event1.getPrincipalId();
		Map<String, String> content = event1.getContent();

		String newId = UUID.randomUUID().toString();
		String currentTime = DateTimeFormatter.ISO_INSTANT
				.format(Instant.now().atOffset(ZoneOffset.UTC));

		Table table = dynamoDb.getTable(DYNAMODB_TABLE_NAME);

		Item item = new Item()
				.withPrimaryKey("id", newId)
				.withInt("principalId", principalId)
				.withString("createdAt", currentTime)
				.withMap("body", content);

		table.putItem(item);

		Event event = Event.builder()
				.id(newId)
				.principalId(principalId)
				.createdAt(currentTime)
				.body(content)
				.build();

		Response response = Response.builder()
				.statusCode(201)
				.event(event)
				.build();

		return response;

	}
}