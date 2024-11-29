package pack;

import databaseServer.Database;

import java.sql.SQLException;

public class Main {
	public static void main(String[] args) throws SQLException {

		int key = Database.getInstance().insertValue("newUsersTable2", "{a:123,b:3710}");

		String returnedValue = Database.getInstance().getValue("newUsersTable2", key);

		System.out.println(returnedValue);

	}
}