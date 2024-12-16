package gilstein.databaseServer;

import gilstein.database.Database;
import gilstein.util.User;
import gilstein.util.DatabaseOutputStream;
import gilstein.util.Pair;
import gilstein.serializer.Serializer;
import gilstein.util.Result;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static gilstein.util.Constants.DEFAULT_PORT;

public class DatabaseManager {

	private static DatabaseManager instance;
	private ServerSocket serverSocket;

	private DatabaseManager() {
		startWaitingForClients();
	}

	private void startWaitingForClients() {
		Thread waitForClients = new Thread(() -> {
			try {
				serverSocket = new ServerSocket(DEFAULT_PORT);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			while (true) {
				try {
					connectToNewClient(serverSocket.accept());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
		waitForClients.start();
	}

	private void connectToNewClient(Socket clientSocket) {
		ClientConnectionThread clientConnectionThread = new ClientConnectionThread(clientSocket);
		clientConnectionThread.start();
	}

	Optional<User> authenticateNewUser(BufferedReader in, DatabaseOutputStream out) throws IOException, SQLException {
		String startMessage = "start connection";
		out.write(startMessage);
		String receivedMessage = in.readLine();
		User user = Serializer.deserialize(receivedMessage, User.class);
		AtomicReference<String> messageToSend = new AtomicReference<>("");
		Optional<User> userToReturn = handleUserVerification(messageToSend, user);
		out.write(messageToSend.get());
		return userToReturn;
	}

	private Optional<User> handleUserVerification(AtomicReference<String> messageToSend, User user) throws SQLException {
		Optional<User> userToReturn;
		if (!Database.getInstance().doesUserExist(user)) {
			user = Database.getInstance().createNewUser(user);
			messageToSend.set("connection established");
			userToReturn = Optional.of(user);
		} else {
			Optional<User> passwordVerifiedUser = checkUserPassword(user);
			passwordVerifiedUser.ifPresentOrElse(
				userWithCorrectPassword -> messageToSend.set("connection established"),
				() -> messageToSend.set("incorrect password")
			);
			userToReturn = passwordVerifiedUser;
		}
		return userToReturn;
	}

	private Optional<User> checkUserPassword(User user) throws SQLException {
		User listedUser = Database.getInstance().getUser(user.userName());
		if (listedUser.password().equals(user.password())) {
			return Optional.of(listedUser);
		} else {
			return Optional.empty();
		}
	}

	void handleInputFromClient(BufferedReader in, DatabaseOutputStream out, User connectedUser) throws IOException, SQLException {
		String receivedMessage = in.readLine();
		switch (receivedMessage.split(" ")[0]) {
			case "getObject" -> handleGetObjectRequest(receivedMessage, out, connectedUser);
			case "getAllObjects" -> handleGetAllObjectsRequest(receivedMessage, out, connectedUser);
			case "insertObject" -> handleInsertObjectRequest(receivedMessage, out, connectedUser);
			case "updateObject" -> handleUpdateObjectRequest(receivedMessage, out);
		}
	}

	private void handleGetObjectRequest(String receivedMessage, DatabaseOutputStream out, User connectedUser) throws IOException, SQLException {
		String[] parts = receivedMessage.split(" ");
		if (Database.getInstance().isKeyInTable(Integer.parseInt(parts[2]))) {
			String object = Database.getInstance().getValue(connectedUser, parts[1], Integer.parseInt(parts[2]));
			out.write(object);
		} else {
			out.sendResultMessage(Result.NOT_A_VALID_KEY);
		}
	}

	private void handleGetAllObjectsRequest(String receivedMessage, DatabaseOutputStream out, User connectedUser) throws IOException, SQLException {
		String[] parts = receivedMessage.split(" ");
		List<Pair<String, Integer>> values = Database.getInstance().getAllValues(connectedUser, parts[1]);
		if (values.isEmpty()) {
			out.sendResultMessage(Result.ERROR);
		} else {
			StringBuilder allValues = new StringBuilder();
			for (Pair<String, Integer> value : values) {
				allValues.append("*").append(value.getFirst()).append("+").append(value.getSecond());
			}
			String allValuesString = allValues.substring(1);
			out.write(allValuesString);
		}
	}

	private void handleInsertObjectRequest(String receivedMessage, DatabaseOutputStream out, User connectedUser) throws IOException, SQLException {
		String[] parts = receivedMessage.split(" ");
		int id = Database.getInstance().insertValue(connectedUser, parts[1], parts[2]);
		out.write(String.valueOf(id));
	}

	private void handleUpdateObjectRequest(String receivedMessage, DatabaseOutputStream out) throws IOException, SQLException {
		System.out.println("updating object");
		String[] parts = receivedMessage.split(" ");
		int id = Integer.parseInt(parts[1]);
		String object = parts[2];
		Database.getInstance().updateValue(id, object);
		out.sendResultMessage(Result.SUCCESS);
		System.out.println("sent success on update");
	}

	public static DatabaseManager getInstance() {
		if (instance == null) {
			instance = new DatabaseManager();
		}
		return instance;
	}

}
