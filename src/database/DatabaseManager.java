package database;

public class DatabaseManager {

	private static DatabaseManager instance;

	private DatabaseManager() {

	}

	public static DatabaseManager getInstance() {
		if (instance == null) {
			instance = new DatabaseManager();
		}
		return instance;
	}

}
