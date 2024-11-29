package gilBase;

import database.User;
import serializer.Serializer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class GilBase {

	private static GilBase instance;
	private Socket socket;
	private BufferedReader in;
	private DataOutputStream out;
	private User user;

	private GilBase() {

	}

	public <T> GilTable<T> getTableReference(String tableName, Class<T> classOfType) {
		if(user == null) {
			throw new NullPointerException("did not log user. all actions must be performed after connecting with user");
		}
		return new GilTable<>(user, tableName, in, out, classOfType);
	}

	public boolean connectUser(User user, String ip, int port) {
		socket = null;
		try {
			socket = initializeSocket(ip, port);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new DataOutputStream(socket.getOutputStream());
			boolean successfulLogin =  logUserIn(user, in, out);
			if(successfulLogin) {
				this.user = user;
			}
			return successfulLogin;
		} catch (IOException e) {
			return false;
		}
	}

	private boolean logUserIn(User user, BufferedReader in, DataOutputStream out) throws IOException {
		if(in.readLine().startsWith("start connection")) {
			System.out.println("it did try to start a connection");
			out.write((Serializer.serialize(user, User.class) + "\n").getBytes());
		}
		else {
			System.out.println("for some reason, it did not ask to start a connection");
		}
		String receivedResult = in.readLine();
		if(receivedResult.startsWith("incorrect password")) {
			System.out.println("the password is incorrect");
			return false;
		}
		else {
			System.out.println("the password is correct");
		}
		return receivedResult.startsWith("connection established");
	}

	private Socket initializeSocket(String ip, int port) {
		Socket socket;
		try {
			socket = new Socket(ip, port);
		} catch (IOException e) {
			throw new RuntimeException("Could not connect to the server");
		}
		return socket;
	}

	public static GilBase getInstance() {
		if (instance == null) {
			instance = new GilBase();
		}
		return instance;
	}

}
