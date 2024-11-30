package pack;

import gilstein.database.User;
import gilstein.gilBase.GilBase;
import gilstein.gilBase.GilTable;
import gilstein.util.Pair;

public class ClientMain {
	public static void main(String[] args) {

		GilBase gilBase = GilBase.getInstance();

		boolean success = gilBase.connectUser(new User("Yuval", "Hello4321"), "localhost").isLoginSuccessful();

		System.out.println("login success: " + success);

		GilTable<Test> table = gilBase.getTableReference("yuval", Test.class);

		table.insertObject(new Test(10400, 5, "ani lo yuval"), id -> System.out.println("the id of the object is: " + id));

		table.getAllObjectsInTable(list -> {
			for (Pair<Test, Integer> s : list) {
				System.out.println(s.getFirst().getA() + ", " + s.getFirst().getB() + ", " + s.getFirst().getC() + " -> " + s.getSecond());
			}
			System.out.println(list.size());
		});

	}
}