package pack;

import database.User;
import gilBase.GilBase;
import gilBase.GilTable;
import gilsteinUtil.Pair;

public class ClientMain {
	public static void main(String[] args) {

		GilBase gilBase = GilBase.getInstance();

		boolean success = gilBase.connectUser(new User("Gil", "Hello1234"), "localhost", 4590);

		System.out.println("login success: " + success);

		GilTable<Test> table = gilBase.getTableReference("gilTable", Test.class);

		table.insertObject(new Test(10, 30), System.out::println);

		table.getAllObjectsInTable(list -> {
			for (Pair<Test, Integer> s : list) {
				System.out.println(s.getFirst().getA() + ", " + s.getFirst().getB() + " -> " + s.getSecond());
			}
			System.out.println(list.size());
		});

	}
}