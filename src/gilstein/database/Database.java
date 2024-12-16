package gilstein.database;

import gilstein.util.Pair;
import gilstein.util.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Database {

	private static Database instance;
	private final Statement statement;

	private Database() {
		String databaseFile = "localdatabase.db";
		try {
			Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
			this.statement = connection.createStatement();
			createTablesIfDontExist();
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

	private void createTablesIfDontExist() throws SQLException {
		String usersTableCreation = """
			CREATE TABLE IF NOT EXISTS tableOfAllUsers (
				id INTEGER,
				userName TEXT PRIMARY KEY,
				password TEXT NOT NULL
			)
			""";
		String valuesTableCreation = """
			CREATE TABLE IF NOT EXISTS tableOfAllValues (
				id INTEGER PRIMARY KEY,
				objectValue TEXT NOT NULL,
				tableName TEXT NOT NULL,
				user_id INTEGER,
				FOREIGN KEY (user_id) REFERENCES tableOfAllUsers(id)
			)
			""";
		Objects.requireNonNull(statement).execute(usersTableCreation);
		Objects.requireNonNull(statement).execute(valuesTableCreation);
	}

	public User createNewUser(User user) throws SQLException {
		int generatedKey = createRandomUserKey();
		String insertCommand = "INSERT INTO tableOfAllUsers (id, userName, password) VALUES (" + generatedKey + ", '" + user.userName() + "', '" + user.password() + "')";
		statement.execute(insertCommand);
		return new User(user.userName(), user.password(), generatedKey);
	}

	public User getUser(String userName) throws SQLException {
		String selectCommand = "SELECT id FROM tableOfAllUsers WHERE userName = '" + userName + "'";
		ResultSet rs = statement.executeQuery(selectCommand);
		int id = rs.getInt("id");
		selectCommand = "SELECT password FROM tableOfAllUsers WHERE userName = '" + userName + "'";
		rs = statement.executeQuery(selectCommand);
		String password = rs.getString("password");
		return new User(userName, password, id);
	}

	public boolean doesUserExist(User user) throws SQLException {
		String selectCommand = "SELECT userName FROM tableOfAllUsers WHERE userName = '" + user.userName() + "'";
		return statement.executeQuery(selectCommand).next();
	}

	private boolean doesUserKeyExist(int userKey) throws SQLException {
		String selectCommand = "SELECT id FROM tableOfAllUsers WHERE id = " + userKey;
		return statement.executeQuery(selectCommand).next();
	}

	public void updateValue(int key, String newValue) throws SQLException {
		if (!isKeyInTable(key)) {
			throw new RuntimeException("key " + key + " does not exist");
		}
		String updateCommand = "UPDATE tableOfAllValues SET objectValue = '" + newValue + "' WHERE id = " + key;
		statement.execute(updateCommand);
	}

	public int insertValue(User user, String table, String value) throws SQLException {
		int generatedKey = createRandomObjectKey();
		String insertCommand = "INSERT INTO tableOfAllValues (id, objectValue, tableName, user_id) VALUES (" + generatedKey + ", '" + value + "', '" + table + "', " + user.id() + ")";
		statement.execute(insertCommand);
		return generatedKey;
	}

	private int createRandomObjectKey() throws SQLException {
		int randomKey = (int) (Math.random() * 1000000000);
		if (isKeyInTable(randomKey)) {
			return createRandomObjectKey();
		}
		return randomKey;
	}

	private int createRandomUserKey() throws SQLException {
		int randomKey = (int) (Math.random() * 1000000000);
		if (doesUserKeyExist(randomKey)) {
			return createRandomUserKey();
		}
		return randomKey;
	}

	public String getValue(User user, String table, int key) throws SQLException {
		String selectCommand = "SELECT objectValue FROM tableOfAllValues WHERE id = " + key + " AND tableName = '" + table + "' AND user_id = " + user.id();
		return statement.executeQuery(selectCommand).getString("objectValue");
	}

	public List<Pair<String, Integer>> getAllValues(User user, String table) throws SQLException {
		String selectCommand = "SELECT objectValue, id FROM tableOfAllValues WHERE tableName = '" + table + "' AND user_id = " + user.id();
		ResultSet rs = statement.executeQuery(selectCommand);
		List<Pair<String, Integer>> values = new ArrayList<>();
		while (rs.next()) {
			values.add(new Pair<>(rs.getString("objectValue"), rs.getInt("id")));
		}
		return values;
	}

	public boolean isKeyInTable(int key) throws SQLException {
		String selectCommand = "SELECT id FROM tableOfAllValues WHERE id = " + key;
		return statement.executeQuery(selectCommand).next();
	}

}
