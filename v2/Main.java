/**
NOTES

Can be proved without computer:
Domain of size 4: every minimally round clone has a binary operation with a subalgebra

TODO: prove LIN cannot be a subalgebra
**/

import java.util.ArrayList;
import java.util.Arrays;
import java.io.*;

class Main {
	public static void write(Object o, String name) {
		try {
			FileOutputStream fos = new FileOutputStream(name);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(o);
			oos.close();
			fos.close();
        } catch(IOException ioe){
			ioe.printStackTrace();
        }
	}
	
	public static Object read(String name) {
		Object o;
		try {
			FileInputStream fis = new FileInputStream(name);
			ObjectInputStream ois = new ObjectInputStream(fis);
			o = ois.readObject();
			ois.close();
			fis.close();
			return o;
		} catch(IOException ioe) {
			ioe.printStackTrace();
		} catch(ClassNotFoundException c){
			System.out.println("Class not found");
			c.printStackTrace();
		}
		return null;
	}
	
	public static int[][] cartesianPower(int[] inputs, int exponent) {
		int[][] product = new int[(int) Math.pow(inputs.length, exponent)][exponent];
		for (int i = 0; i < product.length; i++) {
			for (int j = 0; j < exponent; j++) {
				product[i][j] = inputs[(i / (int) Math.pow(inputs.length, exponent-j-1)) % inputs.length];
			}
		}
		return product;
	}
	
	public static <T> void print(ArrayList<T> arrayList) {
		for (T object : arrayList) {
			System.out.println(object);
		}
	}
	
	public static <T> void print(T[] array) {
		for (T object : array) {
			System.out.println(object);
		}
	}
	
	public static void main(String[] args) {
		Op.setN(4);
		Op o = Op.create(0,2,1,1,3,2);
		System.out.println((new Clone(o)).symmetricOperations(2));
		// Op.setN(5);
		// Op o = Op.create(0,0,1,4,1,3,2,2,4,3);
		// Op p = o.nextOperation();
		// p.setPermutedOutputs(new int[] {1, 2, 3}, 0);
		// p.setPermutedOutputs(new int[] {2, 3, 4}, 3);
		// p.setPermutedOutputs(new int[] {0, 1, 3}, 1);
		// p.setPermutedOutputs(new int[] {1, 2, 4}, 2);
		// Op q = p.nextOperation();
		// System.out.println(o);
		// System.out.println(p);
		// q.setPermutedOutputs(new int[] {0, 0, 3, 3}, 1);
		// q.setPermutedOutputs(new int[] {0, 1, 1, 3}, 1);
		// q.setPermutedOutputs(new int[] {1, 1, 4, 4}, 2);
		// q.setPermutedOutputs(new int[] {1, 2, 2, 4}, 2);
		// q.setPermutedOutputs(new int[] {0, 2, 3, 4}, 1);
		// q = q.convolute(p);
		// System.out.println(q);
		// Clone c = new Clone(o, p, q);
		// Clone.setPrintSymmetric();
		// print(c.symmetricOperations(2));
		//sandbox();
		//createBaseOperations();
	}
	
	public static String convolutionPath(int[] inputs, Op o) {
		String s = "";
		for (int i = 0; i < 4; i++) {
			if (i != 0) s += " -> ";
			for (int d : inputs) {
				s += d;
			}
			inputs = o.convolute(inputs);
		}
		return s;
	}
	
	public static void createBaseOperations() {
		ArrayList<Op> ops = new ArrayList<Op>();
		for (int[] array : cartesianPower(new int[] {0, 1, 2, 3, 4}, 10)) {
			Op o = Op.create(array);
			if (o.count(0) < o.count(1) || o.count(1) < o.count(2) || o.count(2) < o.count(3) || o.count(3) < o.count(4)) {
				continue;
			}
			if (o.unique() == false) {
				continue;
			}
			if (o.contains("LIN") == true) {
				continue;
			}
			boolean exists = false;
			for (Op other : ops) {
				if (other.isomorphic(o)) {
					exists = true;
					break;
				}
			}
			if (exists) {
				continue;
			}
			ops.add(o);
			System.out.println(ops.size());
		}
		write(ops, "domain5uniqueopswithoutLIN");
	}
	
	// 131,134,176,177,178,180,197,209,218,221,268,299,320,326,332,334,377,385,(448),457
	
	public static void sandbox() {
		ArrayList<Op> ops = (ArrayList<Op>) read("special");
		
		for (Op o : ops) {
			System.out.println(o);
			// outer:
			// for (int a = 0; a < 5; a++) {
				// for (int b = 0; b < 5; b++) {
					// if (a == b) continue;
					// if (o.evaluate(a, b) == a) continue;
					// if (o.evaluate(a, b) == b) continue;
					// int[] subalgebra = new int[] {a, b, o.evaluate(a, b)};
					// if (o.isSubalgebra(subalgebra) == false) {
						// System.out.println(o);
						// System.out.println("==============" + o.subalgebras(2).size() + o.subalgebras(3).size() + o.subalgebras(4).size());
						// break outer;
					// }
				// }
			// }
		}
	}
}