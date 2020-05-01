import java.util.ArrayList;

class Main {
	public static void sandbox1() {
		for (int r : roots) {
			if (r < 90) continue;
			System.out.println("===============================================");
			Operation f = equivalenceClasses.get(r);
			System.out.println(r + " : " + string(f));
			
			Clone c = new Clone(f);
			for (int i = 0; i < c.symmetricOperations().size(); i++) {
				ArrayList<Operation> operations = c.symmetricOperations().get(i);
				ArrayList<String> expressions = c.symmetricExpressions().get(i);
				for (int j = 0; j < operations.size(); j++) {
					Operation sym = operations.get(j);
					if (sym.getArity() <= 1) continue;
					System.out.println(expressions.get(j));
					System.out.println(sym.createTable());
				}
			}
			System.out.println(c.totalOperations());
		}
	}
	
	public static void sandbox2() {
		for (int n : roots) {
			Operation o = equivalenceClasses.get(n);
			if (o.compose("f(f(a, b), f(f(a, c), f(b, c)))").isSymmetric() != true || o.compose("f(a, f(b, c))").isSymmetric() == true) {
				continue;
			}
			ArrayList<Operation> operations = new ArrayList<Operation>();
			operations.add(o);
			Operation f2 = o;
			Operation f3 = o.compose("f(f(a, b), f(f(a, c), f(b, c)))");
			Operation f4 = o.compose("f1(f2(a, b, c), f2(f2(a, b, d), f2(a, c, d), f2(b, c, d)))", new Operation[] {f2, f3});
			Operation f5 = o.compose("f1(f2(a, b, c, d), f2(f2(a, b, c, e), f2(a, b, d, e), f2(a, c, d, e), f2(b, c, d, e)))", new Operation[] {f2, f4});
			Operation f6 = o.compose("f1(f2(x1, x2, x3, x4, x5), f2(f2(x2, x3, x4, x5, x6), f2(x1, x3, x4, x5, x6), f2(x1, x2, x4, x5, x6), f2(x1, x2, x3, x5, x6), f2(x1, x2, x3, x4, x6)))", new Operation[] {f2, f5});
			Operation f7 = o.compose("f1(f2(x1, x2, x3, x4, x5, x6), f2(f2(x2, x3, x4, x5, x6, x7), f2(x1, x3, x4, x5, x6, x7), f2(x1, x2, x4, x5, x6, x7), f2(x1, x2, x3, x5, x6, x7), f2(x1, x2, x3, x4, x6, x7), f2(x1, x2, x3, x4, x5, x7)))", new Operation[] {f2, f6});
			operations.add(f3);
			operations.add(f4);
			operations.add(f5);
			operations.add(f6);
			operations.add(f7);
			System.out.println("====================");
			System.out.println(n + " : " + string(o));
			for (Operation op : operations) {
				System.out.println(op.isSymmetric());
				System.out.println(op.createTable());
			}
		}
	}
	
	static ArrayList<Operation> equivalenceClasses;
	static ArrayList<Integer> roots;
	
	public static String string(Operation o) {
		return "f_{" + o.evaluate(0, 1) + o.evaluate(0, 2) + o.evaluate(0, 3) + o.evaluate(1, 2) + o.evaluate(1, 3) + o.evaluate(2, 3) + "}";
	}
	
	public static void main(String[] args) {
		equivalenceClasses = new ArrayList<Operation>();
		ArrayList<String> equivalenceStrings = new ArrayList<String>();
		
		for (int x1 = 0; x1 < 4; x1++) {
			for (int x2 = 0; x2 < 4; x2++) {
				for (int x3 = 0; x3 < 4; x3++) {
					for (int x4 = 0; x4 < 4; x4++) {
						for (int x5 = 0; x5 < 4; x5++) {
							for (int x6 = 0; x6 < 4; x6++) {
								Operation f = Operation.createSymmetricIdempotentOperation(x1, x2, x3, x4, x5, x6);
								boolean exists = false;
								for (int index = 0; index < equivalenceClasses.size(); index++) {
									Operation o = equivalenceClasses.get(index);
									if (f.isEquivalent(o)) {
										String addString = " \\cong " + string(f);
										if ((equivalenceStrings.get(index).length() - equivalenceStrings.get(index).replace("f", "").length()) % 6 == 5) {
											addString += "\\\\";
										}
										equivalenceStrings.set(index, equivalenceStrings.get(index) + addString);
										exists = true;
										break;
									}
								}
								if (exists == false) {
									equivalenceClasses.add(f);
									equivalenceStrings.add("\\begin{gather*}" + string(f));
								}
							}
						}
					}
				}
			}
		}
		
		for (int x = 0; x < 4; x++) {
			Operation o = Operation.createSymmetricIdempotentOperation(0, 0, 3, x, x, x);
			for (Operation p : equivalenceClasses) {
				if (p.isEquivalent(o)) {
					System.out.println(p.createTable());
				}
			}
		}
		
		// for (String s : equivalenceStrings) {
			// if (s.charAt(s.length()-1) == '\\') {
				// s = s.substring(0, s.length() - 2);
			// }
			// System.out.println(s + "\\end{gather*}");
			// System.out.println("\\line");
		// }
		
		roots = new ArrayList<Integer>();
		
		// for (Operation o : equivalenceClasses) {
			// ArrayList<Operation> reduces = new ArrayList<Operation>();
			// for (Operation p : equivalenceClasses) {
				// if (p.compose("f(f(a, f(a, b)), f(b, f(a, b)))").isEquivalent(o)) {
					// reduces.add(p);
				// }
			// }
			// if (reduces.size() != 0) {
				// String s = "";
				// for (Operation q : reduces) {
					// s += string(q) + ", ";
				// }
				// if (s.charAt(s.length() - 1) == ' ') {
					// s = s.substring(0, s.length() - 2);
				// }
				// System.out.println(s + " &\\mapsto " + string(o) + " \\\\");
			// }
		// }
		
		// for (int i = 0; i < equivalenceClasses.size(); i++) {
			// Operation f = equivalenceClasses.get(i);
			// Operation f1 = f.compose("f(f(a, f(a, b)), f(b, f(a, b)))");
			// Operation f2 = f.compose("f(f(a, f(a, f(a, b))), f(b, f(b, f(b, a))))");
			// Operation f3 = f.compose("f(f(a, f(b, f(a, b))), f(b, f(a, f(b, a))))");
			// if (f1.isEquivalent(f) && f2.isEquivalent(f) && f3.isEquivalent(f)) {
				// roots.add(i);
			// }
		// }
		
		// for (int r : roots) {
			// Operation o = equivalenceClasses.get(r);
			// //Operation o = Operation.createSymmetricIdempotentOperation(2, 1, 0, 0, 1, 2);
			// Clone c = new Clone(o);
			// System.out.println("================================");
			// System.out.println(r + ": f_" + o.evaluate(0, 1) + o.evaluate(0, 2) + o.evaluate(0, 3) + o.evaluate(1, 2) + o.evaluate(1, 3) + o.evaluate(2, 3));
			// System.out.println(o.createTable());
			// ArrayList<ArrayList<Operation>> ops = c.symmetricOperations();
			// ArrayList<ArrayList<String>> exps = c.symmetricExpressions();
			// for (int ar = 1; ar < 4; ar++) {
				// for (int i = 0; i < ops.get(ar).size(); i++) {
					// Operation f = ops.get(ar).get(i);
					// String exp = exps.get(ar).get(i);
					// System.out.println(exp);
					// System.out.println(f.createTable());
				// }
			// }
		// }
		
		//sandbox2();
	}
}