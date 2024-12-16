package gilstein.gilBase;

import gilstein.util.DatabaseOutputStream;
import gilstein.util.Pair;
import gilstein.serializer.Serializer;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GilTable<T> {

	private final String tableName;
	private final BufferedReader in;
	private final DatabaseOutputStream out;
	private final Class<T> classOfObject;

	GilTable(String tableName, BufferedReader in, DatabaseOutputStream out, Class<T> classOfObject) {
		this.tableName = tableName;
		this.in = in;
		this.out = out;
		this.classOfObject = classOfObject;
	}

	public void insertObject(T object) {
		String serializedObject = Serializer.serialize(object, object.getClass());
		Thread waitForValue = new Thread(() -> {
			String getMessage = "insertObject " + tableName + " " + serializedObject;
			try {
				out.write(getMessage);
				String receivedLine = in.readLine();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		waitForValue.start();
	}

	public void insertObject(T object, Consumer<Integer> onInsert) {
		String serializedObject = Serializer.serialize(object, object.getClass());
		Thread waitForValue = new Thread(() -> {
			String getMessage = "insertObject " + tableName + " " + serializedObject;
			try {
				out.write(getMessage);
				String receivedLine = in.readLine();
				onInsert.accept(Integer.parseInt(receivedLine));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		waitForValue.start();
	}

	public void updateObject(int id, T object, Runnable onUpdate) {
		String serializedObject = Serializer.serialize(object, object.getClass());
		Thread waitForValue = new Thread(() -> {
			String updateMessage = "updateObject " + id + " " + serializedObject;
			try {
				out.write(updateMessage);
				System.out.println("waiting for input");
				String receivedLine = in.readLine();
				System.out.println("got input");
				onUpdate.run();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		waitForValue.start();
	}

	public void getAllObjectsInTable(Consumer<List<Pair<T, Integer>>> atValueReturned) {
		Thread waitForValue = new Thread(() -> {
			String getMessage = "getAllObjects " + tableName;
			try {
				out.write(getMessage);
				String receivedLine = in.readLine();
				if (!receivedLine.startsWith("error")) {
					String[] parts = receivedLine.split("\\*");
					List<Pair<T, Integer>> objects = new ArrayList<>();
					for(String part : parts) {
						String[] valueAndId = part.split("\\+");
						objects.add(new Pair<>(Serializer.deserialize(valueAndId[0], classOfObject), Integer.parseInt(valueAndId[1])));
					}
					atValueReturned.accept(objects);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		waitForValue.start();
	}

	public void getObject(int id, Consumer<T> atValueReturned) {
		Thread waitForValue = new Thread(() -> {
			String getMessage = "getObject " + tableName + " " + id;
			try {
				out.write(getMessage);
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
