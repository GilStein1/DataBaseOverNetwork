package databaseServer;

import java.sql.*;
import java.util.Objects;

public class Database {

	private static Database instance;
	private final Statement statement;

	private Database() {
		String databaseFile = "localdatabase.db";
		try {
			Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
			createUserTableIfNotExists();
			this.statement = connection.createStatement();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private void createUserTableIfNotExists() throws SQLException {
		String usersTableCreation = """
			CREATE TABLE IF NOT EXISTS tableOfAllUsers (
				id INTEGER PRIMARY KEY,
				userName TEXT NOT NULL,
				password TEXT NOT NULL
			)
			""";
		String valuesTableCreation = """
			CREATE TABLE IF NOT EXISTS tableOfAllValues (
				id INTEGER PRIMARY KEY,
				objectValue TEXT NOT NULL,
				tableName TEXT NOT NULL,
				user_id INTEGER
				FOREIGN KEY (user_id) REFERENCES tableOfAllUsers(id)
			)
			""";
		Objects.requireNonNull(statement).execute(usersTableCreation);
		Objects.requireNonNull(statement).execute(valuesTableCreation);
	}

	public static Database getInstance() {
		if (instance == null) {
			instance = new Database();
		}
		return instance;
	}

	public void updateValue(int key, String newValue) throws SQLException {
		if(!isKeyInTable(key)) {
			throw new RuntimeException("key " + key + " does not exist");
		}
		String updateCommand = "UPDATE tableOfAllValues SET value = '" + newValue + "' WHERE id = " + key;
		statement.execute(updateCommand);
	}

	public int insertValue(User user, String table, String value) throws SQLException {
		int generatedKey = createRandomKey();
		String insertCommand = "INSERT INTO tableOfAllValues (id, objectValue, tableName, user_id) VALUES (" + generatedKey + ", '" + value + "', '" + table + "', " + user.id() + ")";
		statement.execute(insertCommand);
		return generatedKey;
	}

	private int createRandomKey() throws SQLException {
		int randomKey = (int)(Math.random() * 1000000);
		if(isKeyInTable(randomKey)) {
			return createRandomKey();
		}
		return randomKey;
	}

	public String getValue(User user, String table, int key) throws SQLException {
		String selectCommand = "SELECT value FROM tableOfAllValues WHERE id = " + key + " AND tableName = '" + table + "' AND user_id = " + user.id();
		ResultSet rs = statement.executeQuery(selectCommand);
		return rs.getString("value");
	}

	private boolean isKeyInTable(int key) throws SQLException {
		String selectCommand = "SELECT id FROM tableOfAllValues WHERE id = " + key;
		ResultSet rs = statement.executeQuery(selectCommand);
		return rs.next();
	}

}
