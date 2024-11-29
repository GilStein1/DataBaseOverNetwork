package database;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;

public class DatabaseManager {

	private static DatabaseManager instance;
	private ServerSocket serverSocket;

	private DatabaseManager() {



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
			try {
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				out = new DataOutputStream(clientSocket.getOutputStream());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			boolean isConnected = true;
			while (!clientSocket.isClosed() && isConnected) {
				handleInputFromClient(in);
				sendOutputToClient(out);
			}
			try {
				clientSocket.close();
			} catch (IOException ignored) {}
		});
		clientThread.start();
	}

	private void handleInputFromClient(BufferedReader in) {

	}

	private void sendOutputToClient(DataOutputStream out) {

	}

	public static DatabaseManager getInstance() {
		if (instance == null) {
			instance = new DatabaseManager();
		}
		return instance;
	}

}
