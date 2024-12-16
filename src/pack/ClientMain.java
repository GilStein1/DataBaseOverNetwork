package pack;

import gilstein.util.User;
import gilstein.gilBase.GilBase;
import gilstein.gilBase.GilTable;
import gilstein.util.Pair;

public class ClientMain {
	public static void main(String[] args) {

		User user = new User("newUser", "secret password");

		GilBase gilBase = GilBase.getInstance();

		gilBase.connectUser(user, "localhost");

		GilTable<Test> testTable = gilBase.getTableReference("testTable", Test.class);

		Test t = new Test(7654, 235, "gdhfjsdhjk");

		testTable.insertObject(t ,(id) -> {
//			System.out.println("inserted the object with id " + id);
		});

		testTable.updateObject(507373861, new Test(0, 0, "no"), () -> {
			System.out.println("update");
//			testTable.getAllObjectsInTable(list -> {
//				for(Pair<Test, Integer> pair : list) {
//					System.out.println(pair.getFirst().getC() + ", the id is " + pair.getSecond());
//				}
//			});
		});

		System.out.println("getting all objects");

//		testTable.getAllObjectsInTable(list -> {
//			for(Pair<Test, Integer> pair : list) {
//				System.out.println(pair.getFirst().getC() + ", the id is " + pair.getSecond());
//			}
//		});

	}
}