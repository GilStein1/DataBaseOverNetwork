package gilstein.databaseServer;

import gilstein.database.Database;
import gilstein.database.User;
import gilstein.util.Pair;
import gilstein.serializer.Serializer;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
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

	Optional<User> authenticateNewUser(BufferedReader in, DataOutputStream out) throws IOException, SQLException {
		String startMessage = "start connection\n";
		out.write(startMessage.getBytes());
		String receivedMessage = in.readLine();
		User user = Serializer.deserialize(receivedMessage, User.class);
		if (!Database.getInstance().doesUserExist(user)) {
			user = Database.getInstance().createNewUser(user);
		} else {
			User listedUser = Database.getInstance().getUser(user.userName());
			if (listedUser.password().equals(user.password())) {
				user = listedUser;
			} else {
				String wrongPasswordMessage = "incorrect password\n";
				out.write(wrongPasswordMessage.getBytes());
				return Optional.empty();
			}
		}
		String connectionEstablishedMessage = "connection established\n";
		out.write(connectionEstablishedMessage.getBytes());
		return Optional.of(user);
	}

	void handleInputFromClient(BufferedReader in, DataOutputStream out, User connectedUser) throws IOException, SQLException {
		String receivedMessage = in.readLine();
		if (receivedMessage.startsWith("getObject")) {
			String[] parts = receivedMessage.split(" ");
			if (Database.getInstance().isKeyInTable(Integer.parseInt(parts[2]))) {
				String object = Database.getInstance().getValue(connectedUser, parts[1], Integer.parseInt(parts[2]));
				out.write((object + "\n").getBytes());
			} else {
				out.write("notAValidKey\n".getBytes());
			}
		} else if (receivedMessage.startsWith("getAllObjects")) {
			String[] parts = receivedMessage.split(" ");
			List<Pair<String, Integer>> values = Database.getInstance().getAllValues(connectedUser, parts[1]);
			if (values.isEmpty()) {
				out.write("error\n".getBytes());
			} else {
				StringBuilder allValues = new StringBuilder();
				for (Pair<String, Integer> value : values) {
					allValues.append("*").append(value.getFirst()).append("+").append(value.getSecond());
				}
				String allValuesString = allValues.append("\n").substring(1);
				out.write(allValuesString.getBytes());
			}
		} else if (receivedMessage.startsWith("insertObject")) {
			String[] parts = receivedMessage.split(" ");
			int id = Database.getInstance().insertValue(connectedUser, parts[1], parts[2]);
			out.write((id + "\n").getBytes());
		} else if (receivedMessage.startsWith("updateObject")) {
			String[] parts = receivedMessage.split(" ");
			int id = Integer.parseInt(parts[1]);
			String object = parts[2];
			Database.getInstance().updateValue(id, object);
			out.write("success\n".getBytes());
		}
	}

	public static DatabaseManager getInstance() {
		if (instance == null) {
			instance = new DatabaseManager();
		}
		return instance;
	}

}
