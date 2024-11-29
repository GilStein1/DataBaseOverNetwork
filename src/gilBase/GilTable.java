package gilBase;

import database.User;
import serializer.Serializer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Consumer;

public class GilTable {

	private final String tableName;
	private final User user;
	private final BufferedReader in;
	private final DataOutputStream out;

	GilTable(User user, String tableName, BufferedReader in, DataOutputStream out) {
		this.tableName = tableName;
		this.user = user;
		this.in = in;
		this.out = out;
	}

	public <T> void insertObject(T object, Consumer<Integer> onInsert) {
		String serializedObject = Serializer.serialize(object, object.getClass());
		Thread waitForValue = new Thread(() -> {
			String getMessage = "insertObject " + tableName + " " + serializedObject + "\n";
			try {
				out.write(getMessage.getBytes());
				String receivedLine = in.readLine();
				onInsert.accept(Integer.parseInt(receivedLine));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		waitForValue.start();
	}

	public <T> void getObject(int id, Class<T> classOfObject, Consumer<T> atValueReturned) {
		Thread waitForValue = new Thread(() -> {
			String getMessage = "getObject " + tableName + " " + id + "\n";
			try {
				out.write(getMessage.getBytes());
				String receivedLine = in.readLine();
				if(!receivedLine.startsWith("notAValidKey")) {
					atValueReturned.accept(Serializer.deserialize(receivedLine, classOfObject));
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		waitForValue.start();
	}

}
