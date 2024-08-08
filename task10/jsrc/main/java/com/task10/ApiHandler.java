package com.task10;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.fasterxml.jackson.databind.ObjectMapper;


@LambdaHandler(
    lambdaName = "api_handler",
	roleName = "api_handler-role",
	isPublishVersion =false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@EnvironmentVariables({
		@EnvironmentVariable(key = "tables_table",value = "${tables_table}"),
		@EnvironmentVariable(key = "reservations_table",value = "${reservations_table}"),
		@EnvironmentVariable(key = "booking_userpool",value = "${booking_userpool}")
})
public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
	private static final String USER_POOL_ID = "simple-booking-userpool";
	private static final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
	private static final DynamoDB dynamoDb = new DynamoDB(client);
	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static final AWSCognitoIdentityProvider cognitoClient = AWSCognitoIdentityProviderClientBuilder.defaultClient();

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
		String path = request.getPath();
		String httpMethod = request.getHttpMethod();
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

		try {
			switch (path) {
				case "/signup":
					if ("POST".equals(httpMethod)) {
						return handleSignUp(request);
					}
					break;
				case "/signin":
					if ("POST".equals(httpMethod)) {
						return handleSignIn(request);
					}
					break;
				case "/tables":
					if ("GET".equals(httpMethod)) {
						return handleGetTables(request);
					} else if ("POST".equals(httpMethod)) {
						return handlePostTable(request);
					}
					break;
				case "/reservations":
					if ("GET".equals(httpMethod)) {
						return handleGetReservations(request);
					} else if ("POST".equals(httpMethod)) {
						return handlePostReservation(request);
					}
					break;
				default:
					response.setStatusCode(404);
					return response;
			}
		} catch (Exception e) {
			response.setStatusCode(500);
			response.setBody("{ \"error\": \"" + e.getMessage() + "\" }");
			return response;
		}

		response.setStatusCode(400);
		return response;
	}

	private APIGatewayProxyResponseEvent handleSignUp(APIGatewayProxyRequestEvent request) throws Exception {
		// Extract the body from the request and convert it to the User class
		User user = objectMapper.readValue(request.getBody(), User.class);

		// Validate the user details
		if (!isValidEmail(user.getEmail()) || !isValidPassword(user.getPassword())) {
			throw new IllegalArgumentException("Invalid email or password");
		}

		// Create a Cognito user
		AdminCreateUserRequest cognitoRequest = new AdminCreateUserRequest()
				.withUserPoolId(USER_POOL_ID)
				.withUsername(user.getEmail())
				.withUserAttributes(
						new AttributeType().withName("email").withValue(user.getEmail()),
						new AttributeType().withName("custom:firstName").withValue(user.getFirstName()),
						new AttributeType().withName("custom:lastName").withValue(user.getLastName())
				)
				.withMessageAction("SUPPRESS");

		AdminCreateUserResult result = cognitoClient.adminCreateUser(cognitoRequest);

		// Prepare the response
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		response.setStatusCode(200);
		response.setBody("{ \"message\": \"User registered successfully.\" }");
		return response;
	}

	private APIGatewayProxyResponseEvent handleSignIn(APIGatewayProxyRequestEvent request) throws Exception {
		// Extract the body from the request and convert it to the User class
		User user = objectMapper.readValue(request.getBody(), User.class);

		// Validate the user details
		if (!isValidEmail(user.getEmail()) || !isValidPassword(user.getPassword())) {
			throw new IllegalArgumentException("Invalid email or password");
		}

		// Authenticate the user with Cognito
		SignUpRequest signUpRequest = new SignUpRequest()
				.withClientId(USER_POOL_ID)
				.withUsername(user.getEmail())
				.withPassword(user.getPassword());

		SignUpResult signUpResult = cognitoClient.signUp(signUpRequest);

		// Prepare the response
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		response.setStatusCode(200);
		response.setBody("{ \"accessToken\": \"" + signUpResult.getUserSub() + "\" }");
		return response;
	}

	private APIGatewayProxyResponseEvent handleGetTables(APIGatewayProxyRequestEvent request) throws Exception {
		Table table = dynamoDb.getTable("Tables");
		Item item = table.getItem("id", 1); // Example to get an item

		// Prepare the response
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		response.setStatusCode(200);
		response.setBody("{ \"tables\": [" + item.toJSON() + "] }");
		return response;
	}

	private APIGatewayProxyResponseEvent handlePostTable(APIGatewayProxyRequestEvent request) throws Exception {
		Table table = dynamoDb.getTable("Tables");
		Item item = new Item().withPrimaryKey("id", 1); // Example to create an item
		table.putItem(item);

		// Prepare the response
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		response.setStatusCode(200);
		response.setBody("{ \"id\": 1 }");
		return response;
	}

	private APIGatewayProxyResponseEvent handleGetReservations(APIGatewayProxyRequestEvent request) throws Exception {
		Table table = dynamoDb.getTable("Reservations");
		Item item = table.getItem("id", 1); // Example to get an item

		// Prepare the response
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		response.setStatusCode(200);
		response.setBody("{ \"reservations\": [" + item.toJSON() + "] }");
		return response;
	}

	private APIGatewayProxyResponseEvent handlePostReservation(APIGatewayProxyRequestEvent request) throws Exception {
		Table table = dynamoDb.getTable("Reservations");
		Item item = new Item().withPrimaryKey("id", 1); // Example to create an item
		table.putItem(item);

		// Prepare the response
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		response.setStatusCode(200);
		response.setBody("{ \"reservationId\": \"uuid-generated-id\" }");
		return response;
	}

	private boolean isValidEmail(String email) {
		return email != null && email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
	}

	private boolean isValidPassword(String password) {
		return password != null && password.matches("^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[$%^*]).{12,}$");
	}
}

class User {
	private String firstName;
	private String lastName;
	private String email;
	private String password;

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}