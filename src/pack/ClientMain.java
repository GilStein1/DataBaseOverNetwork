package pack;

import database.User;
import gilBase.GilBase;
import gilBase.GilTable;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientMain {
	public static void main(String[] args) throws InterruptedException {

		GilBase gilBase = GilBase.getInstance();

		boolean success = gilBase.connectUser(new User("Gil", "Hello1234"), "localhost", 4590);

		System.out.println("login success: " + success);

		GilTable<Test> table = gilBase.getTableReference("gilTable", Test.class);

		AtomicInteger ID = new AtomicInteger();

//		table.insertObject(new Test(70, 28), (id) -> {
//			System.out.println(id);
//			ID.set(id);
//			table.getObject(id, value -> {
//			});
//		});

		Thread.sleep(1000);

		table.getAllObjectsInTable(list -> {
			for(Test s : list) {
				System.out.println(s.getA() + ", " + s.getB());
			}
			System.out.println(list.size());
		});

	}
}