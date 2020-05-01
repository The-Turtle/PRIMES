import java.util.ArrayList;
import java.util.Arrays;

// only finds binary and ternary functions
class Clone {
	public static void main(String[] args) {
		Operation o = Operation.createSymmetricIdempotentOperation(0, 0, 1, 3, 0, 0);
		Clone c = new Clone(o);
		System.out.println(c.operations.get(2).size());
	}
	
	private static final int MAX_ARITY = 2;
	private static final int MAX_OPERATIONS = 1000;
	
	private static int[] getTuple(int max, int exponent, int i) {
		int[] tuple = new int[exponent];
		for (int j = 0; j < tuple.length; j++) {
			tuple[j] = i / (int) Math.pow(max, exponent - j - 1) % max;
		}
		return tuple;
	}
	
	public ArrayList<ArrayList<Operation>> operations = new ArrayList<ArrayList<Operation>>();
	private ArrayList<ArrayList<String>> expressions = new ArrayList<ArrayList<String>>();
	
	public Clone(Operation... generators) {
		for (int ar = 0; ar <= MAX_ARITY; ar++) {
			operations.add(new ArrayList<Operation>());
			expressions.add(new ArrayList<String>());
			for (int i = 1; i <= ar; i++) {
				operations.get(ar).add(Operation.projection(ar, i));
				expressions.get(ar).add("x" + i);
			}
		}
		boolean done = false;
		boolean warning = false;
		outer:
		while (done == false) {
			done = true;
			for (int ar = 1; ar <= MAX_ARITY; ar++) {
				for (int genNumber = 0; genNumber < generators.length; genNumber++) {
					Operation gen = generators[genNumber];
					// creates a string of the form "f4(f1(x1, x2), f2(x1, x2), f3(x1, x2))"
					String s = "(";
					for (int i = 1; i <= ar; i++) {
						s += "x" + i;
						if (i != ar) s += ", ";
					}
					s += ")";
					String expression = "f" + (gen.getArity() + 1) + "(";
					for (int i = 1; i <= gen.getArity(); i++) {
						expression += "f" + i + s;
						if (i != gen.getArity()) expression += ", ";
					}
					expression += ")";
					for (int k = 0; k < (int) Math.pow(operations.get(ar).size(), gen.getArity()); k++) {
						int[] tuple = getTuple(operations.get(ar).size(), gen.getArity(), k);
						Operation[] operationList = new Operation[gen.getArity() + 1];
						String genExpression = "f" + (generators.length == 1 ? "" : (genNumber+1)) + "(";
						for (int i = 0; i < gen.getArity(); i++) {
							operationList[i] = operations.get(ar).get(tuple[i]);
							genExpression += expressions.get(ar).get(tuple[i]);
							if (i != gen.getArity() - 1) genExpression += ", ";
						}
						genExpression += ")";
						operationList[gen.getArity()] = gen;
						Operation f = Operation.compose(expression, operationList, ar);
						boolean existsAlready = false;
						for (int opIndex = 0; opIndex < operations.get(ar).size(); opIndex++) {
							Operation o = operations.get(ar).get(opIndex);
							if (f.equals(o)) {
								existsAlready = true;
								if (genExpression.length() < expressions.get(ar).get(opIndex).length()) {
									expressions.get(ar).set(opIndex, genExpression);
								}
								break;
							}
						}
						if (existsAlready == false) {
							operations.get(ar).add(f);
							expressions.get(ar).add(genExpression);
							if (f.isSymmetric()) {
								System.out.println(f.createTable());
								System.out.println(genExpression);
							}
							if (operations.get(ar).size() % 10 == 0) System.out.println(operations.get(ar).size());
							done = false;
							
						}
						if (operations.get(MAX_ARITY).size() > MAX_OPERATIONS) {
							done = true;
							warning = true;
							System.out.println("WARNING: clone not completed");
							break outer;
						}
					}
				}
			}
			if (warning == true) {
				//System.out.println("WARNING: clone not completed");
			}
		}
	}
	
	public ArrayList<Integer> totalOperations() {
		ArrayList<Integer> totals = new ArrayList<Integer>();
		for (ArrayList<Operation> ops : operations) {
			totals.add(ops.size());
		}
		return totals;
	}
	
	public ArrayList<Operation> symmetricOperations(int arity) {
		ArrayList<Operation> symmetricOperations = new ArrayList<Operation>();
		for (Operation o : operations.get(arity)) {
			if (o.isSymmetric()) {
				symmetricOperations.add(o);
			}
		}
		return symmetricOperations;
	}
	
	public ArrayList<String> symmetricExpressions(int arity) {
		ArrayList<String> symmetricExpressions = new ArrayList<String>();
		for (int i = 0; i < operations.get(arity).size(); i++) {
			Operation o = operations.get(arity).get(i);
			if (o.isSymmetric()) {
				symmetricExpressions.add(expressions.get(arity).get(i));
			}
		}
		return symmetricExpressions;
	}
	
	public ArrayList<ArrayList<Operation>> symmetricOperations() {
		ArrayList<ArrayList<Operation>> symmetricOperations = new ArrayList<ArrayList<Operation>>();
		for (int arity = 0; arity <= MAX_ARITY; arity++) {
			symmetricOperations.add(symmetricOperations(arity));
		}
		return symmetricOperations;
	}
	
	public ArrayList<ArrayList<String>> symmetricExpressions() {
		ArrayList<ArrayList<String>> symmetricExpressions = new ArrayList<ArrayList<String>>();
		for (int arity = 0; arity <= MAX_ARITY; arity++) {
			symmetricExpressions.add(symmetricExpressions(arity));
		}
		return symmetricExpressions;
	}
	
	@Override
	public String toString() {
		String s = "";
		for (int ar = 1; ar <= MAX_ARITY; ar++) {
			s += "ARITY " + ar + ":" + "\n";
			for (int i = 0; i < operations.get(ar).size(); i++) {
				Operation o = operations.get(ar).get(i);
				s += expressions.get(ar).get(i) + "\n";
				s += o.createTable();
				s += "\n";
			}
		}
		return s;
	}
}