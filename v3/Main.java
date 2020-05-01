import java.util.Arrays;

class Main {
	public static final int n = SymOp.n;
	
	public static void verify(Clone c, int depth) {
		if (c.depth() == 2 || c.depth() == 3) {
			//c.override(1);
			//c.override(1);
			//c.override(1);
			//c.override(2);
			//c.override(3);
		}
		if (c.depth() > n) {
			System.out.println("=============================");
			System.out.println(c.getOp(2));
			System.out.println(c.getOp(3));
			System.out.println(c.getOp(4));
			throw new RuntimeException("FAILURE");
		}
		boolean successful = true;
		for (int counter = 0; counter < depth; counter++) {
			if (c.baseOp().initialized()) {
				c.generate();
			} else {
				for (int[] input : c.baseOp().undefinedInputs()) {
					System.out.println(Arrays.toString(input));
				}
				successful = false;
				break;
			}
		}
		if (!successful) {
			for (SymOp p : c.possibleNextOperations()) {
				Clone d = c.clone();
				d.addOp(p);
				verify(d, depth);
			}
		} else {
			System.out.println(c.depth() + " terminated");
		}
	}
	
	public static void main(String[] args) {
		// for (int[] tuple : Tools.cartesianPower(new int[] {0, 1, 2, 3}, 6)) {
			// if (Tools.count(tuple, 0) < Tools.count(tuple, 1)) continue;
			// if (Tools.count(tuple, 1) < Tools.count(tuple, 2)) continue;
			// if (Tools.count(tuple, 2) < Tools.count(tuple, 3)) continue;
			// SymOp o = new SymOp(tuple);
			// if (o.containsLin()) continue;
			// System.out.println(o.table());
			// verify(new Clone(o), 8);
		// }
		SymOp o = new SymOp(0,0,1,4,1,3,2,2,4,3);
		SymOp p = (new Clone(o)).baseOp();
		p.setOutput(new int[] {1, 2, 3}, 0);
		p.setOutput(new int[] {2, 3, 4}, 3);
		System.out.println(p.toString());
		verify(new Clone(o, p), 8);
	}
}