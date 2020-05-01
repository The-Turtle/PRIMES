import java.util.Arrays;
import java.util.ArrayList;

class Operation {
	private static final int N = 4; // domain of size 4
	private static final int MAX_ARITY = 7; // maximum possible arity of any Operation
	private static final int[][][] possibleInputs; // for memoization; possibleInputs[n] is the array of all n-tuples
	private static final ArrayList<int[]> permutations; // for memoization; permutations of [1, 2, ..., n]
	
	public static void main(String[] args) {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				Operation o = createSymmetricIdempotentOperation(0, 0, 3, 1, 1, 2);
				// 001, 002, 003, 011, 012, 013, 022, 023, 033, 112, 113, 122, 123, 133, 223, 233
				Operation rps3 = createSymmetricIdempotentOperation(0, 0, 3, 0, 0, i, 0, j, 3, 1, 1, 1, 1, 1, 2, 2);
				Operation rps2 = o;
				Operation s4 = compose("f1(a, f2(f1(a, b), f1(a, c), f1(a, d)))", new Operation[] {rps2, rps3});
				Operation rps4 = compose("f1(a, f2(a, b, c, d), f2(f2(a, b, c, d), b, c, d))", new Operation[] {rps3, s4});
				Operation s5 = compose("f1(a, f2(f1(a, b), f1(a, c), f1(a, d), f1(a, e)))", new Operation[] {rps2, rps4});
				Operation rps5 = compose("f1(a, f2(a, b, c, d, e), f2(f2(a, b, c, d, e), b, c, d, e))", new Operation[] {rps3, s5});
				//System.out.println("" + i + j + rps4.isSymmetric() + rps5.isSymmetric());
				if (true) {
					System.out.println(rps3.createTable());
					Operation test = rps3.compose("f(f(a, a, f(a, b, c)), f(b, b, f(a, b, c)), f(c, c, f(a, b, c)))");
					System.out.println(test.isSymmetric());
					System.out.println(test.createTable());
				}
			}
		}
		
	}
	
	private static int factorial(int n) {
		if (n < 0) throw new RuntimeException(n + "! is not defined");
		int product = 1;
		for (int i = 1; i <= n; i++) {
			product *= i;
		}
		return product;
	}
	
	private static int choose(int n, int k) {
		if (n < 0 || k < 0 || n-k < 0) return 0;
		int product = 1;
		for (int i = n; i > n-k; i--) {
			product *= i;
		}
		product /= factorial(k);
		return product;
	}
	
	// returns all permutations of an array
	private static ArrayList<int[]> permutations(int[] array) {
		ArrayList<int[]> permutations = new ArrayList<int[]>();
		if (array.length == 0) return permutations;
		if (array.length == 1) {
			permutations.add(array);
			return permutations;
		}
		for (int n = 0; n < array.length; n++) {
			boolean appearedBefore = false;
			for (int i = 0; i < n; i++) {
				if (array[n] == array[i]) {
					appearedBefore = true;
					break;
				}
			}
			if (appearedBefore == true) continue;
			int[] remainingNumbers = new int[array.length - 1];
			for (int i = 0; i < array.length; i++) {
				if (i < n) remainingNumbers[i] = array[i];
				if (i > n) remainingNumbers[i-1] = array[i];
			}
			for (int[] subPermutation : permutations(remainingNumbers)) {
				int[] permutation = new int[array.length];
				permutation[0] = array[n];
				for (int i = 1; i < array.length; i++) {
					permutation[i] = subPermutation[i - 1];
				}
				permutations.add(permutation);
			}
		}
		return permutations;
	}
	
	// converts 0 <= n < N^arity to its base N representation
	private static int[] intToArray(int n, int arity) {
		if (n < 0) throw new RuntimeException(n + " out of bounds");
		if (n >= Math.pow(N, arity)) throw new RuntimeException(n + " out of bounds");
		int[] array = new int[arity];
		for (int i = 0; i < arity; i++) {
			array[i] = (n / (int) Math.pow(N, arity - i - 1)) % N;
		}
		return array;
	}
	
	// converts an arity-digit base N number to an integer
	private static int arrayToInt(int[] array, int arity) {
		if (array.length != arity) throw new RuntimeException("array length " + array.length + " not equal to arity " + arity);
		int index = 0;
		for (int n : array) {
			index = N * index + n;
		}
		return index;
	}
	
	// evaluates an expression, like f1(f2(x1, x2), x2), at a given input
	private static int evaluateExpression(String expression, Operation[] operations, int[] inputs) {
		expression = expression.trim();
		if (expression.contains("f") == false) {
			if (expression.charAt(0) != 'x') throw new RuntimeException("Unknown expression: " + expression);
			int n;
			try {
				n = Integer.parseInt(expression.substring(1, expression.length())) - 1;
			} catch (Exception e) {
				throw new RuntimeException("Unknown expression: " + expression);
			}
			if (n < 0 || n >= inputs.length) throw new RuntimeException(expression + " has more than " + inputs.length + " variables ");
			return inputs[n];
		}
		if (expression.charAt(0) != 'f') throw new RuntimeException("Unknown expression: " + expression);
		if (expression.contains("(") == false) throw new RuntimeException("Unknown expression: " + expression);
		if (expression.charAt(expression.length() - 1) != ')') throw new RuntimeException("Unknown expression: " + expression);
		int n;
		try {
			n = Integer.parseInt(expression.substring(1, expression.indexOf('('))) - 1;
		} catch (Exception e) {
			throw new RuntimeException("Unknown expression: " + expression);
		}
		if (n < 0 || n >= operations.length) throw new RuntimeException("More operations than available");
		Operation o = operations[n];
		String argumentString = expression.substring(expression.indexOf('(') + 1, expression.length() - 1);
		ArrayList<String> arguments = new ArrayList<String>();
		int parenthesesCount = 0;
		int commaIndex = -1;
		for (int index = 0; index < argumentString.length(); index++) {
			switch (argumentString.charAt(index)) {
				case '(':
					parenthesesCount++;
					break;
				case ')':
					parenthesesCount--;
					break;
				case ',':
					if (parenthesesCount == 0) {
						arguments.add(argumentString.substring(commaIndex+1, index));
						commaIndex = index;
					}
				default: break;
			}
			if (parenthesesCount < 0) throw new RuntimeException("Unmatched parentheses: " + expression);
		}
		arguments.add(argumentString.substring(commaIndex+1, argumentString.length()));
		if (arguments.size() != o.getArity()) throw new RuntimeException("Unmatched arities: " + expression);
		int[] evaluatedArguments = new int[o.getArity()];
		for (int i = 0; i < evaluatedArguments.length; i++) {
			evaluatedArguments[i] = evaluateExpression(arguments.get(i), operations, inputs);
		}
		return o.evaluate(evaluatedArguments);
	}
	
	// returns an Operation that is the composition of other Operations
	public static Operation compose(String expression, Operation[] operations) {
		if (expression.contains("x") == false) {
			expression = expression.replaceAll("a", "x1");
			expression = expression.replaceAll("b", "x2");
			expression = expression.replaceAll("c", "x3");
			expression = expression.replaceAll("d", "x4");
			expression = expression.replaceAll("e", "x5");
		}
		String parsed = expression.replaceAll("[^fx0123456789]", "");
		String s = "";
		char lastLetter = ' ';
		for (int i = 0; i < parsed.length(); i++) {
			if (parsed.charAt(i) == 'f') lastLetter = 'f';
			if (parsed.charAt(i) == 'x') lastLetter = 'x';
			if (lastLetter == 'x') s += parsed.charAt(i);
		}
		String[] numbers = s.split("x");
		int arity = 0;
		for (String n : numbers) {
			if (n.length() == 0) continue;
			arity = Math.max(Integer.parseInt(n), arity);
		}
		return compose(expression, operations, arity);
	}
	
	public static Operation compose(String expression, Operation[] operations, int arity) {
		if (arity < 0 || arity > MAX_ARITY) throw new RuntimeException("Invalid arity: " + arity);
		int[] outputs = new int[(int) Math.pow(N, arity)];
		for (int[] input : possibleInputs[arity]) {
			outputs[arrayToInt(input, arity)] = evaluateExpression(expression, operations, input);
		}
		return new Operation(outputs);
	}
	
	// returns a projection function
	public static Operation projection(int arity, int index) {
		int[] outputs = new int[(int) Math.pow(N, arity)];
		for (int[] input : possibleInputs[arity]) {
			outputs[arrayToInt(input, arity)] = input[index-1];
		}
		return new Operation(outputs);
	}
	
	// returns a symmetric idempotent operation given the values it takes on tuples sorted in numeric order
	// for example the function min(a, b) would be created by passing the tuple (0, 0, 0, 1, 1, 2)
	public static Operation createSymmetricIdempotentOperation(int... compressedOutputs) {
		int arity = -1;
		for (int i = 1; i <= MAX_ARITY; i++) {
			if (choose(i+N-1, N-1) - N == compressedOutputs.length) {
				arity = i;
				break;
			}
		}
		if (arity == -1) throw new RuntimeException("arity not initialized; array of length " + compressedOutputs.length);
		int[] outputs = new int[(int) Math.pow(N, arity)];
		int[] array = new int[arity];
		for (int n : compressedOutputs) {
			boolean allEqual = true;
			for (int i = 1; i < arity; i++) {
				if (array[i-1] != array[i]) allEqual = false;
			}
			if (allEqual == true) {
				array[arity-1]++;
			}
			for (int[] permutation : permutations(array)) {
				outputs[arrayToInt(permutation, arity)] = n;
			}
			int lastNonMaximalIndex = -1;
			for (int i = arity-1; i >= 0; i--) {
				if (array[i] != N-1) {
					lastNonMaximalIndex = i;
					break;
				}
			}
			array[lastNonMaximalIndex]++;
			for (int i = lastNonMaximalIndex+1; i < arity; i++) {
				array[i] = array[lastNonMaximalIndex];
			}
		}
		for (int n = 0; n < N; n++) {
			array = new int[arity];
			for (int i = 0; i < arity; i++) {
				array[i] = n;
			}
			outputs[arrayToInt(array, arity)] = n;
		}
		return new Operation(outputs);
	}
	
	// initializes possibleInputs and permutations
	static {
		possibleInputs = new int[MAX_ARITY+1][][];
		for (int arity = 0; arity <= MAX_ARITY; arity++) {
			possibleInputs[arity] = new int[(int) Math.pow(N, arity)][arity];
			for (int i = 0; i < (int) Math.pow(N, arity); i++) {
				possibleInputs[arity][i] = intToArray(i, arity);
			}
		}
		int[] numbers = new int[N];
		for (int i = 0; i < N; i++) {
			numbers[i] = i;
		}
		permutations = permutations(numbers);
	}
	
	private int arity;
	private int[] outputs;
	
	public Operation(int[] outputs) {
		this.arity = -1;
		for (int i = 0; i <= MAX_ARITY; i++) {
			if ((int) Math.pow(N, i) == outputs.length) {
				this.arity = i;
				break;
			}
		}
		if (this.arity == -1) throw new RuntimeException("arity not initialized; array of length " + outputs.length);
		this.outputs = outputs;
	}
	
	// returns the set of all possible input arrays to this operation
	private int[][] possibleInputs() {
		return possibleInputs[arity];
	}
	
	public int getArity() {
		return arity;
	}
	
	public int[] getOutputs() {
		return outputs;
	}
	
	public int evaluate(int... inputs) {
		return outputs[arrayToInt(inputs, arity)];
	}
	
	// returns a function that can be created by composing this operation
	// for example, f(a, f(c, b))
	public Operation compose(String expression) {
		return compose(expression.replaceAll("f", "f1"), new Operation[] {this});
	}
	
	public boolean isSymmetric() {
		for (int[] input : possibleInputs()) {
			int[] swap = new int[arity];
			int[] cycle = new int[arity];
			for (int position = 0; position < arity; position++) {
				if (arity >= 2) swap[position] = input[(position <= 1) ? (1 - position) : position];
				else swap[position] = input[position];
				cycle[position] = input[(position + 1) % arity];
			}
			if (evaluate(input) != evaluate(swap) || evaluate(input) != evaluate(cycle)) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isIdempotent() {
		for (int i = 0; i < N; i++) {
			int[] input = new int[arity];
			for (int j = 0; j < arity; j++) input[j] = i;
			if (evaluate(input) != i) return false;
		}
		return true;
	}
	
	// equivalent under renaming of variables
	public boolean isEquivalent(Operation o) {
		if (o.getArity() != this.getArity()) return false;
		for (int[] permutation : permutations) {
			boolean equivalent = true;
			for (int[] input : possibleInputs()) {
				int[] modifiedInput = new int[arity];
				for (int i = 0; i < arity; i++) {
					modifiedInput[i] = permutation[input[i]];
				}
				if (permutation[this.evaluate(input)] != o.evaluate(modifiedInput)) {
					equivalent = false;
					break;
				}
			}
			if (equivalent == true) {
				return true;
			}
		}
		return false;
	}
	
	// creates a text table
	public String createTable() {
		String s = "";
		switch (arity) {
			case 1:
				s += " |";
				for (int i = 0; i < N; i++) s += i;
				s += "\n";
				s += "-+";
				for (int i = 0; i < N; i++) s += "-";
				s += "\n";
				s += " |";
				for (int i = 0; i < N; i++) s += evaluate(i);
				s += "\n";
				break;
			case 2:
				s += " |";
				for (int i = 0; i < N; i++) s += i;
				s += "\n";
				s += "-+";
				for (int i = 0; i < N; i++) s += "-";
				s += "\n";
				for (int i = 0; i < N; i++) {
					s += i + "|";
					for (int j = 0; j < N; j++) {
						s += evaluate(i, j);
					}
					s += "\n";
				}
				break;
			case 3:
				s += " |";
				for (int i = 0; i < N*N; i++) {
					if (i % N == 0 && i != 0) {
						s += " ";
					}
					s += i / N;
				}
				s += "\n";
				s += " |";
				for (int i = 0; i < N*N; i++) {
					if (i % N == 0 && i != 0) {
						s += " ";
					}
					s += i % N;
				}
				s += "\n";
				s += "-+";
				for (int i = 0; i < N*N + N - 1; i++) s += "-";
				s += "\n";
				for (int i = 0; i < N; i++) {
					s += i + "|";
					for (int j = 0; j < N; j++) {
						for (int k = 0; k < N; k++) {
							s += evaluate(i, j, k);
						}
						s += " ";
					}
					s += "\n";
				}
				break;
			case 4:
				s += "  |";
				for (int i = 0; i < N*N; i++) {
					if (i % N == 0 && i != 0) {
						s += " ";
					}
					s += i / N;
				}
				s += "\n";
				s += "  |";
				for (int i = 0; i < N*N; i++) {
					if (i % N == 0 && i != 0) {
						s += " ";
					}
					s += i % N;
				}
				s += "\n";
				s += "--+";
				for (int i = 0; i < N*N + N - 1; i++) s += "-";
				s += "\n";
				for (int i = 0; i < N; i++) {
					for (int j = 0; j < N; j++) {
						s += "" + i + j + "|";
						for (int k = 0; k < N; k++) {
							for (int l = 0; l < N; l++) {
								s += evaluate(i, j, k, l);
							}
							s += " ";
						}
						s += "\n";
					}
					if (i != N-1) s += "\n";
				}
				break;
			default:
				s += "could not create table";
		}
		return s;
	}
	
	@Override
	public String toString() {
		String string = "";
		for (int[] array : possibleInputs()) {
			string += "f(";
			for (int position = 0; position < arity; position++) {
				if (position != 0) string += ",";
				string += array[position];
			}
			string += ")=" + evaluate(array) + "; ";
		}
		return string;
	}
	
	@Override
	public boolean equals(Object o) {
        if (o == this) return true;
        if ((o instanceof Operation) == false) return false;
		if (((Operation) o).getArity() != this.getArity()) return false;
		for (int i = 0; i < outputs.length; i++) {
			if (outputs[i] != ((Operation) o).getOutputs()[i]) return false;
		}
		return true;
	}
}