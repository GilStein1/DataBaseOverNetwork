package databaseServer;

import database.Database;
import database.User;
import serializer.Serializer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;

public class DatabaseManager {

	private static DatabaseManager instance;
	private ServerSocket serverSocket;

	private DatabaseManager() {
		startWaitingForClients(4590);
	}

	private void startWaitingForClients(int port) {
		Thread waitForClients = new Thread(() -> {
			try {
				serverSocket = new ServerSocket(port);
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
		Thread clientThread = new Thread(() -> {
			BufferedReader in;
			DataOutputStream out;
			User connectedUser;
			try {
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				out = new DataOutputStream(clientSocket.getOutputStream());
				connectedUser = authenticateNewUser(in, out);
				if (connectedUser == null) {
					clientSocket.close();
				}
			} catch (IOException | SQLException e) {
				throw new RuntimeException(e);
			}
			boolean isConnected = true;
			while (!clientSocket.isClosed() && isConnected) {
				try {
					handleInputFromClient(in, out, connectedUser);
				} catch (IOException | SQLException ignored) {
				}
			}
			try {
				clientSocket.close();
			} catch (IOException ignored) {
			}
		});
		clientThread.start();
	}

	private User authenticateNewUser(BufferedReader in, DataOutputStream out) throws IOException, SQLException {
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
				return null;
			}
		}
		String connectionEstablishedMessage = "connection established\n";
		out.write(connectionEstablishedMessage.getBytes());
		return user;
	}

	private void handleInputFromClient(BufferedReader in, DataOutputStream out, User connectedUser) throws IOException, SQLException {
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
			List<String> values = Database.getInstance().getAllValues(connectedUser, parts[1]);
			if (values.isEmpty()) {
				out.write("error\n".getBytes());
			} else {
				StringBuilder allValues = new StringBuilder();
				for (String value : values) {
					allValues.append("*").append(value);
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
