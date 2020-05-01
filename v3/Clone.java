import java.util.Arrays;
import java.util.ArrayList;

class Clone {
	public static final int curlLimit = 12;
	
	public final int n = SymOp.n;
	
	private final ArrayList<SymOp> symOps = new ArrayList<SymOp>();
	
	public Clone(SymOp... generators) {
		for (SymOp op : generators) {
			if (op.arity != symOps.size() + 2) {
				throw new RuntimeException("symmetric operations must be passed in order");
			}
			symOps.add(op);
		}
	}
	
	public SymOp getOp(int arity) {
		return symOps.get(arity - 2);
	}
	
	public void setOp(SymOp o) {
		symOps.set(o.arity-2, o);
	}
	
	public SymOp lastOp() {
		return symOps.get(symOps.size() - 1);
	}
	
	public void addOp(SymOp op) {
		if (op.arity != symOps.size() + 2) {
			throw new RuntimeException("symmetric operations must be added in order");
		}
		symOps.add(op);
	}
	
	public int depth() {
		return symOps.size() + 1;
	}
	
	public int evaluate(int... inputs) {
		return getOp(inputs.length).evaluate(inputs);
	}
	
	public int[] evaluate(int[]... inputs) {
		return getOp(inputs.length).evaluate(inputs);
	}
	
	public int[] curl(int[] tuple) {
		return getOp(tuple.length - 1).curl(tuple);
	}
	
	public int[] curl(int[] tuple, int iterations) {
		if (iterations <= 0) return tuple;
		return curl(curl(tuple, iterations - 1));
	}
	
	public String curlPath(int[] tuple) {
		String s = "";
		for (int d : tuple) s += d;
		for (int i = 0; i < curlLimit; i++) {
			s += "->";
			tuple = curl(tuple);
			for (int d : tuple) s += d;
		}
		return s;
	}
	
	public int[] sgnStrategy(int[] tuple) {
		return evaluate(tuple, curl(tuple));
	}
	
	public int[] rpsStrategy(int[] tuple) {
		int[] result = new int[tuple.length];
		for (int i = 0; i < result.length; i++) {
			int[] tuple1 = tuple;
			int x1 = tuple1[i];
			int[] tuple2 = evaluate(Tools.repeat(x1, tuple.length), tuple);
			tuple2 = curl(tuple2, 2);
			int x2 = tuple2[i];
			int[] tuple3 = evaluate(Tools.repeat(x2, tuple.length), tuple);
			tuple3 = curl(tuple3, 2);
			int x3 = tuple3[i];
			result[i] = evaluate(x1, x2, x3);
		}
		return result;
	}
	
	public void override(int iterations) {
		// replaces fn with fn(f2(a, ... f2(a, abc...)), ...)
		SymOp o = new SymOp(depth());
		for (int[] input: SymOp.allInputs(depth())) {
			int[] filteredInput = input;
			if (depth() >= 3) filteredInput = sgnStrategy(curl(input, curlLimit));
			int[] tuple = Tools.repeat(evaluate(filteredInput), depth());
			for (int i = 0; i < iterations; i++) {
				tuple = evaluate(filteredInput, tuple);
			}
			int result = evaluate(tuple);
			o.setOutput(input, result);
		}
		setOp(o);
	}
	
	public SymOp baseOp() {
		return baseOp(symOps.size() + 2);
	}
	
	public SymOp baseOp(int arity) {
		SymOp base = new SymOp(arity);
		for (int[] input: SymOp.allInputs(arity)) {
			int[] tuple = sgnStrategy(curl(input, curlLimit));
			if (arity > 3) tuple = rpsStrategy(tuple);
			if (Tools.allEqual(tuple)) {
				base.setOutput(input, tuple[0]);
			} else {
				base.setOutput(input, -1);
			}
		}
		return base;
	}
	
	public SymOp generate() {
		SymOp base = baseOp();
		if (!base.initialized()) {
			ArrayList<int[]> uninitialized = base.undefinedInputs();
			String error = "";
			for (int[] input : uninitialized) {
				error += Arrays.toString(input) + " ";
			}
			error += "could not be assigned outputs";
			for (int[] input : uninitialized) {
				error += "\n" + curlPath(input);
			}
			throw new RuntimeException(error);
		}
		symOps.add(base);
		return base;
	}
	
	public Clone clone() {
		SymOp[] generators = new SymOp[symOps.size()];
		for (int i = 0; i < symOps.size(); i++) {
			generators[i] = symOps.get(i);
		}
		return new Clone(generators);
	}
	
	public ArrayList<SymOp> possibleNextOperations() {
		SymOp base = baseOp();
		ArrayList<SymOp> symOps = new ArrayList<SymOp>();
		ArrayList<int[]> inputs = base.undefinedInputs();
		if (inputs.size() == 0) {
			symOps.add(base);
			return symOps;
		}
		ArrayList<int[]> prev = inputs;
		ArrayList<int[]> next = new ArrayList<int[]>();
		for (int i = 0; i < prev.size(); i++) {
			int[] curlResult = curl(prev.get(i));
			Arrays.sort(curlResult);
			next.add(curlResult);
		}
		while (Tools.distinctArrays(prev) != Tools.distinctArrays(next)) {
			prev = next;
			for (int i = 0; i < prev.size(); i++) {
				int[] curlResult = curl(prev.get(i));
				Arrays.sort(curlResult);
				next.set(i, curlResult);
			}
		}
		int[] equivalences = new int[inputs.size()];
		int counter = 0;
		for (int i = 0; i < equivalences.length; i++) {
			boolean exists = false;
			for (int j = 0;  j < i; j++) {
				if (Arrays.equals(next.get(j), next.get(i))) {
					equivalences[i] = equivalences[j];
					exists = true;
					break;
				}
			}
			if (!exists) {
				equivalences[i] = counter;
				counter++;
			}
		}
		for (int[] tuple : Tools.cartesianPower(Tools.createDomain(n), 1+Tools.max(equivalences))) {
			SymOp o = base.clone();
			for (int i = 0; i < inputs.size(); i++) {
				o.setOutput(inputs.get(i), tuple[equivalences[i]]);
			}
			symOps.add(o);
		}
		return symOps;
	}

	public String toString() {
		String s = "";
		for (SymOp op : symOps) {
			s += op;
		}
		return s;
	}
}