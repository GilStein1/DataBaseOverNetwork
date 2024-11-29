package databaseServer;

import java.sql.*;

public class Database {

	private static Database instance;
	private final Statement statement;

	private Database() {
		String databaseFile = "localdatabase.db";
		try {
			Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
			this.statement = connection.createStatement();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public static Database getInstance() {
		if (instance == null) {
			instance = new Database();
		}
		return instance;
	}

	public void updateValue(String table, int key, String newValue) throws SQLException {
		if(!isKeyInTable(table, key)) {
			throw new RuntimeException("key " + key + " not in table " + table);
		}
		String updateCommand = "UPDATE " + table + " SET value = '" + newValue + "' WHERE id = " + key;
		statement.execute(updateCommand);
	}

	public int insertValue(String table, String value) throws SQLException {
		createTableIfNotExists(table);
		int generatedKey = createRandomKey(table);
		String insertCommand = "INSERT INTO " +  table + "(id, value) VALUES (" + generatedKey + ", '" + value + "')";
		statement.execute(insertCommand);
		return generatedKey;
	}

	private int createRandomKey(String table) throws SQLException {
		int randomKey = (int)(Math.random() * 1000000);
		if(isKeyInTable(table, randomKey)) {
			return createRandomKey(table);
		}
		return randomKey;
	}

	public String getValue(String table, int key) throws SQLException {
		String selectCommand = "SELECT value FROM " +  table + " WHERE id = " + key;
		ResultSet rs = statement.executeQuery(selectCommand);
		return rs.getString("value");
	}

	private boolean isKeyInTable(String table, int key) throws SQLException {
		String selectCommand = "SELECT id FROM " + table + " WHERE id = " + key;
		ResultSet rs = statement.executeQuery(selectCommand);
		return rs.next();
	}

	private void createTableIfNotExists(String tableName) throws SQLException {
		String command = """
			CREATE TABLE IF NOT EXISTS NAME_OF_TABLE (
				id INTEGER PRIMARY KEY,
			    value TEXT NOT NULL
			)
			""";
		statement.execute(command.replace("NAME_OF_TABLE", tableName));
	}

}
