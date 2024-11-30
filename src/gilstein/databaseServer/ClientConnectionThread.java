package gilstein.databaseServer;

import gilstein.database.User;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Optional;

public class ClientConnectionThread {

	private final Thread thread;
	private final Socket clientSocket;
	private final DatabaseManager databaseManager;
	private final BufferedReader in;
	private final DataOutputStream out;
	private User connectedUser;

	ClientConnectionThread(Socket clientSocket) {
		this.thread = new Thread(this::init);
		this.clientSocket = clientSocket;
		this.databaseManager = DatabaseManager.getInstance();
		try {
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			out = new DataOutputStream(clientSocket.getOutputStream());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void start() {
		thread.start();
	}

	private void init() {
		try {
			Optional<User> user = databaseManager.authenticateNewUser(in, out);
			user.ifPresent(connectedUser -> this.connectedUser = connectedUser);
			if(user.isEmpty()) {
				clientSocket.close();
			}
		} catch (IOException | SQLException e) {
			throw new RuntimeException(e);
		}
		startConnectionLoop();
	}


	private void startConnectionLoop() {
		while (!clientSocket.isClosed()) {
			try {
				databaseManager.handleInputFromClient(in, out, connectedUser);
			} catch (IOException | SQLException ignored) {
			}
		}
	}

}