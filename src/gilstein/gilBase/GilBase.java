package gilstein.gilBase;

import gilstein.database.User;
import gilstein.serializer.Serializer;
import gilstein.util.DatabaseOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import static gilstein.util.Constants.DEFAULT_PORT;

public class GilBase {

	private static GilBase instance;
	private BufferedReader in;
	private DatabaseOutputStream out;
	private User user;

	private GilBase() {

	}

	public <T> GilTable<T> getTableReference(String tableName, Class<T> classOfType) {
		if (user == null) {
			throw new NullPointerException("did not log user. all actions must be performed after connecting with user");
		}
		return new GilTable<>(tableName, in, out, classOfType);
	}

	public boolean connectUser(User user, String ip) {
		Socket socket;
		try {
			socket = initializeSocket(ip);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new DatabaseOutputStream(socket.getOutputStream());
			boolean successfulLogin = logUserIn(user, in, out);
			if (successfulLogin) {
				this.user = user;
			}
			return successfulLogin;
		} catch (IOException e) {
			return false;
		}
	}

	private boolean logUserIn(User user, BufferedReader in, DatabaseOutputStream out) throws IOException {
		if (in.readLine().startsWith("start connection")) {
			out.write(Serializer.serialize(user, User.class));
		}
		String receivedResult = in.readLine();
		if (receivedResult.startsWith("incorrect password")) {
			return false;
		}
		return receivedResult.startsWith("connection established");
	}

	private Socket initializeSocket(String ip) {
		Socket socket;
		try {
			socket = new Socket(ip, DEFAULT_PORT);
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
