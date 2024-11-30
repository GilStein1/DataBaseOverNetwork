package gilstein.gilBase;

import gilstein.database.User;
import gilstein.util.Pair;
import gilstein.serializer.Serializer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GilTable<T> {

	private final String tableName;
	private final User user;
	private final BufferedReader in;
	private final DataOutputStream out;
	private final Class<T> classOfObject;

	GilTable(User user, String tableName, BufferedReader in, DataOutputStream out, Class<T> classOfObject) {
		this.tableName = tableName;
		this.user = user;
		this.in = in;
		this.out = out;
		this.classOfObject = classOfObject;
	}

	public void insertObject(T object, Consumer<Integer> onInsert) {
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

	public void getAllObjectsInTable(Consumer<List<Pair<T, Integer>>> atValueReturned) {
		Thread waitForValue = new Thread(() -> {
			String getMessage = "getAllObjects " + tableName + "\n";
			try {
				out.write(getMessage.getBytes());
				String receivedLine = in.readLine();
				if (!receivedLine.startsWith("error")) {
					String[] parts = receivedLine.split("\\*");
					List<Pair<T, Integer>> objects = new ArrayList<>();
					for(String part : parts) {
						String[] valueAndId = part.split("\\+");
						objects.add(new Pair<>(Serializer.deserialize(valueAndId[0], classOfObject), Integer.parseInt(valueAndId[1])));
					}
					atValueReturned.accept(objects);
//					atValueReturned.accept(
//						deserializeList(receivedLine.split("\\*"))
//					);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		waitForValue.start();
	}

	private List<T> deserializeList(String[] arr) {
		List<T> list = new ArrayList<>();
		for (String s : arr) {
			list.add(Serializer.deserialize(s, classOfObject));
		}
		return list;
	}

	public void getObject(int id, Consumer<T> atValueReturned) {
		Thread waitForValue = new Thread(() -> {
			String getMessage = "getObject " + tableName + " " + id + "\n";
			try {
				out.write(getMessage.getBytes());
				String receivedLine = in.readLine();
				if (!receivedLine.startsWith("notAValidKey")) {
					atValueReturned.accept(Serializer.deserialize(receivedLine, classOfObject));
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		waitForValue.start();
	}

}
