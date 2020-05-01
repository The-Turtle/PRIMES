import java.util.ArrayList;
import java.util.Arrays;

class Op implements java.io.Serializable {
	public final static int MAX_ARITY = 5;
	public final static int MAX_DOMAIN_SIZE = 5;
	public final static int DEFAULT_DOMAIN_SIZE = 4;
	private final static String DEFAULT_NAME = "f";
	private final static char DEFAULT_VARIABLE = 'x';
	private final static int CONVOLUTION_THRESHOLD = 10;
	private static int currentN;
	
	private static ArrayList<int[][]> bijections = new ArrayList<int[][]>();
	private static ArrayList<Op[][]> projections = new ArrayList<Op[][]>();
	private static ArrayList<Op[][]> constants = new ArrayList<Op[][]>();
	private static ArrayList<Op> namedAlgebras = new ArrayList<Op>();
	
	static {
		// for memoization
		bijections.add(new int[0][0]);
		projections.add(new Op[0][0]);
		constants.add(new Op[0][0]);
		for (int n = 1; n <= MAX_DOMAIN_SIZE; n++) {
			int[] domain = new int[n];
			for (int i = 0; i < domain.length; i++) {
				domain[i] = i;
			}
			ArrayList<int[]> perms = permutations(domain);
			int[][] currentBijections = new int[perms.size()][n];
			for (int i = 0; i < currentBijections.length; i++) {
				currentBijections[i] = perms.get(i);
			}
			bijections.add(currentBijections);
			Op[][] currentProjections = new Op[MAX_ARITY][];
			for (int arity = 1; arity <= MAX_ARITY; arity++) {
				currentProjections[arity-1] = new Op[arity];
				for (int i = 1; i <= arity; i++) {
					int[] outputs = new int[(int) Math.pow(n, arity)];
					for (int index = 0; index < outputs.length; index++) {
						outputs[index] = (index / (int) Math.pow(n, arity - i)) % n;
					}
					Op o = new Op(outputs, n);
					o.setName("pi_" + i + "^" + arity);
					o.setExpression("x" + i);
					currentProjections[arity-1][i-1] = o;
				}
			}
			projections.add(currentProjections);
			Op[][] currentConstants = new Op[MAX_ARITY][n];
			for (int arity = 1; arity <= MAX_ARITY; arity++) {
				for (int d = 0; d < n; d++) {
					int[] outputs = new int[(int) Math.pow(n, arity)];
					for (int index = 0; index < outputs.length; index++) {
						outputs[index] = d;
					}
					Op o = new Op(outputs, n);
					o.setName("c_" + d + "^" + arity);
					o.setExpression("" + d);
					currentConstants[arity-1][d] = o;
				}
			}
			constants.add(currentConstants);
		}
		
		setN(1);
		Op unitary = create(); unitary.setName("UNI");
		
		setN(2);
		Op binary = create(0); unitary.setName("UNI");
		
		setN(3);
		Op free = create(0, 0, 0); free.setName("FRE");
		Op min = create(0, 0, 1); min.setName("MIN");
		Op sign = create(0, 1, 2); sign.setName("SGN");
		Op rockPaperScissors = create(0, 2, 1); rockPaperScissors.setName("RPS");
		Op linear = create(2, 1, 0); linear.setName("LIN");
		
		namedAlgebras.add(unitary);
		namedAlgebras.add(binary);
		namedAlgebras.add(free);
		namedAlgebras.add(min);
		namedAlgebras.add(sign);
		namedAlgebras.add(rockPaperScissors);
		namedAlgebras.add(linear);
		
		setN(DEFAULT_DOMAIN_SIZE);
	}
	
	public static void setN(int newN) {
		if (newN <= 0) throw new RuntimeException("Domain size must be positive");
		if (newN > MAX_DOMAIN_SIZE) throw new RuntimeException("Domain size must be less than or equal to " + MAX_DOMAIN_SIZE);
		currentN = newN;
	}
	
	public static ArrayList<Op> getNamedOperations() {
		return namedAlgebras;
	}
	
	private static ArrayList<int[]> permutations(int[] inputs) {
		return permutations(inputs, inputs.length - 1);
	}
	
	private static ArrayList<int[]> permutations(int[] inputs, int k) {
		ArrayList<int[]> perms = new ArrayList<int[]>();
		if (k == 0) {
			perms.add(inputs);
			return perms;
		}
		for (int i = 0; i <= k; i++) {
			int index = 0;
			while (inputs[index] != inputs[i]) {
				index++;
			}
			if (index == i) {
				int[] swap = inputs.clone();
				int temp = swap[i];
				swap[i] = swap[k];
				swap[k] = temp;
				perms.addAll(permutations(swap, k-1));
			}
		}
		return perms;
	}
	
	private static boolean allEqual(int[] array) {
		for (int i = 1; i < array.length; i++) {
			if (array[i-1] != array[i]) return false;
		}
		return true;
	}
	
	// creates a symmetric idempotent operation
	public static Op create(int... compressedInputs) {
		if (currentN == 0) throw new RuntimeException("setN() has not been called");
		int arity = 0;
		for (int ar = 1; ar <= MAX_ARITY; ar++) {
			int inputSize = 1;
			for (int i = currentN; i <= currentN+ar-1; i++) inputSize *= i;
			for (int i = 1; i <= ar; i++) inputSize /= i;
			inputSize -= currentN;
			if (inputSize == compressedInputs.length) {
				arity = ar;
				break;
			}
		}
		if (arity == 0) {
			throw new RuntimeException(compressedInputs.length + " compressed outputs not possible with a domain of size " + currentN);
		}
		int[] counter = new int[arity];
		Op o = new Op(new int[(int) Math.pow(currentN, arity)]);
		for (int input : compressedInputs) {
			int lastNonmaximalIndex = counter.length - 1;
			while (counter[lastNonmaximalIndex] == currentN - 1) {
				lastNonmaximalIndex--;
			}
			for (int index = counter.length - 1; index >= lastNonmaximalIndex; index--) {
				counter[index] = counter[lastNonmaximalIndex] + 1;
			}
			if (allEqual(counter) == true) {
				counter[counter.length - 1]++;
			}
			for (int[] perm : permutations(counter)) {
				o.setOutput(perm, input);
			}
		}
		for (int k = 0; k < currentN; k++) {
			for (int i = 0; i < counter.length; i++) {
				counter[i] = k;
			}
			o.setOutput(counter, k);
		}
		return o;
	}
	
	// creates a function that returns the ith input (1-indexed)
	public static Op projection(int arity, int i) {
		return projection(arity, i, currentN);
	}
	
	private static Op projection(int arity, int i, int d) {
		return projections.get(d)[arity-1][i-1];
	}
	
	// creates a constant function
	public static Op constant(int arity, int d) {
		return constants.get(currentN)[arity-1][d];
	}
	
	private static Op compose(String s, Op[] ops, int arity) {
		// base case: projection functions and constants
		if (s.indexOf("(") == -1) {
			if (s.charAt(0) != DEFAULT_VARIABLE) {
				return constant(arity, Integer.parseInt(s));
			} else {
				return projection(arity, Integer.parseInt(s.substring(1, s.length())));
			}
		}
		// parse inputs into arguments
		String outerOperationName = s.substring(0, s.indexOf("("));
		ArrayList<String> parsedStrings = new ArrayList<String>();
		int parenthesesCount = 0;
		int lastCommaIndex = s.indexOf("(");
		for (int index = s.indexOf("(")+1; index < s.length()-1; index++) {
			switch (s.charAt(index)) {
				case '(':
					parenthesesCount++;
					break;
				case ')':
					parenthesesCount--;
					break;
				case ',':
					if (parenthesesCount == 0) {
						parsedStrings.add(s.substring(lastCommaIndex+1, index));
						lastCommaIndex = index;
					}
				default:
					break;
			}
		}
		parsedStrings.add(s.substring(lastCommaIndex+1, s.length()-1));
		if (parenthesesCount != 0) {
			throw new RuntimeException(s + " has unmatched parentheses");
		}
		Op outerOperation = null;
		for (Op o : ops) {
			if (o.getName().equals(outerOperationName)) {
				outerOperation = o;
				break;
			}
		}
		if (outerOperation == null) {
			throw new RuntimeException("Unknown Operation: " + outerOperationName);
		}
		if (outerOperation.getArity() != parsedStrings.size()) {
			throw new RuntimeException("Arities do not match: " + s);
		}
		Op[] insideOperations = new Op[parsedStrings.size()];
		for (int i = 0; i < insideOperations.length; i++) {
			insideOperations[i] = compose(parsedStrings.get(i), ops, arity);
		}
		Op composedOperation = outerOperation.compose(insideOperations);
		return composedOperation;
	}
	
	public static Op compose(String s, Op[] ops) {
		int ar;
		s = s.replaceAll("\\s", "");
		ArrayList<String> letters = new ArrayList<String>(); 
		for (char letter : "abcdefghijklmnopqrstuvwxyz".toCharArray()) {
			if (s.indexOf(letter + ")") != -1 || s.indexOf(letter + ",") != -1) {
				letters.add("" + letter);
			}
		}
		for (int index = 0; index < s.length() - 1; index++) {
			String currentChar = "" + s.charAt(index);
			if (letters.contains(currentChar) && (s.charAt(index+1) == ',' || s.charAt(index+1) == ')')) {
				s = s.substring(0, index) + "x" + (letters.indexOf(currentChar)+1) + s.substring(index+1, s.length());
			}
		}
		if (letters.size() != 0) {
			ar = letters.size();
		} else {
			ar = 0;
			int currentNumber = 0;
			boolean currentlyCounting = false;
			for (int index = 0; index < s.length()-1; index++) {
				char currentChar = s.charAt(index);
				switch (currentChar) {
					case 'x':
						currentNumber = 0;
						currentlyCounting = true;
						break;
					case '0': currentNumber = 10 * currentNumber + 0; break;
					case '1': currentNumber = 10 * currentNumber + 1; break;
					case '2': currentNumber = 10 * currentNumber + 2; break;
					case '3': currentNumber = 10 * currentNumber + 3; break;
					case '4': currentNumber = 10 * currentNumber + 4; break;
					case '5': currentNumber = 10 * currentNumber + 5; break;
					case '6': currentNumber = 10 * currentNumber + 6; break;
					case '7': currentNumber = 10 * currentNumber + 7; break;
					case '8': currentNumber = 10 * currentNumber + 8; break;
					case '9': currentNumber = 10 * currentNumber + 9; break;
					default:
						if (currentlyCounting) {
							ar = Math.max(ar, currentNumber);
						}
						currentNumber = 0;
						currentlyCounting = false;
				}
			}
		}
		return compose(s, ops, ar);
	}
	
	private static int[] repeat(int n, int times) {
		int[] array = new int[times];
		for (int i = 0; i < times; i++) {
			array[i] = n;
		}
		return array;
	}

	public static Op[] generateSymmetricOperations(Op... generators) {
		Op[] symmetricOperations = new Op[MAX_ARITY + 1];
		int n = generators[0].getN();
		symmetricOperations[1] = projection(1, 1);
		for (Op sym : generators) {
			if (sym.symmetric() == false) {
				throw new RuntimeException("generator operations must be symmetric");
			}
			if (sym.getN() != n) {
				throw new RuntimeException("generators must all be over the same domain");
			}
			int arity = sym.getArity();
			if (symmetricOperations[arity] != null) {
				throw new RuntimeException("only one generator operation for each arity allowed");
			}
			symmetricOperations[arity] = sym;
		}
		if (symmetricOperations[2] == null) {
			throw new RuntimeException("binary operation need to generate all symmetric operations");
		}
		int startingArity = 0;
		for (int ar = 2; true; ar++) {
			if (symmetricOperations[ar] == null) {
				startingArity = ar;
				break;
			}
		}
		for (int arity = startingArity; arity <= MAX_ARITY; arity++) {
			String errorString = "Cannot create arity " + arity + " operation from operations " + generators[0].getName();
			for (int i = 1; i < generators.length; i++) {
				errorString += ", " + generators[i].getName();
			}
			int[] inputs = new int[(int) Math.pow(n, arity)];
			for (int inputIndex = 0; inputIndex < inputs.length; inputIndex++) {
				int[] input = new int[arity];
				for (int i = 0; i < input.length; i++) {
					input[i] = (inputIndex / (int) Math.pow(n, arity - i - 1)) % n;
				}
				// convolute as much as possible
				input = symmetricOperations[arity-1].convolute(input, CONVOLUTION_THRESHOLD);
				// SGN strategy
				input = symmetricOperations[2].evaluate(input, symmetricOperations[arity-1].convolute(input));
				// RPS strategy
				if (symmetricOperations[3] != null) {
					int[] nextTuple = new int[arity];
					for (int i = 0; i < nextTuple.length; i++) {
						int[] a = symmetricOperations[2].evaluate(input, repeat(input[i], arity));
						a = symmetricOperations[arity-1].convolute(a, CONVOLUTION_THRESHOLD);
						int[] b = symmetricOperations[2].evaluate(input, repeat(a[i], arity));
						b = symmetricOperations[arity-1].convolute(b, CONVOLUTION_THRESHOLD);
						int[] c = symmetricOperations[2].evaluate(input, repeat(b[i], arity));
						c = symmetricOperations[arity-1].convolute(c, CONVOLUTION_THRESHOLD);
						nextTuple[i] = symmetricOperations[3].evaluate(a, b, c)[i];
					}
					input = nextTuple;
				}
				if (allEqual(input) == false) {
					throw new RuntimeException(errorString);
				}
				inputs[inputIndex] = input[0];
			}
			Op sym = new Op(inputs);
			if (sym.symmetric() == false) {
				throw new RuntimeException("developer error: strategies are not safe");
			}
			symmetricOperations[arity] = sym;
		}
		Op[] shiftedArray = new Op[symmetricOperations.length - 1];
		for (int i = 0; i < shiftedArray.length; i++) {
			shiftedArray[i] = symmetricOperations[i+1];
		}
		return shiftedArray;
	}
	
	private final int n;
	private final int arity;
	private int[] outputs;
	private String name;
	private String expression;
	private ArrayList<int[]> subalgebraList;
	
	public Op(int[] outputs) {
		this(outputs, currentN);
	}
	
	private Op(int[] outputs, int n) {
		this.n = n;
		int possibleArity = 0;
		for (int ar = 1; ar <= MAX_ARITY; ar++) {
			if ((int) Math.pow(n, ar) == outputs.length) {
				possibleArity = ar;
				break;
			}
		}
		if (possibleArity == 0) {
			throw new RuntimeException(outputs.length + " outputs not possible with a domain of size " + n);
		}
		this.arity = possibleArity;
		for (int i = 0; i < outputs.length; i++) {
			if ((outputs[i] < 0 || outputs[i] >= n) && outputs[i] != -1) {
				throw new RuntimeException("output " + outputs[i] + " not possible with a domain of size " + n);
			}
		}
		this.outputs = outputs;
		this.name = DEFAULT_NAME;
		this.expression = this.name + "(";
		for (int i = 1; i <= arity; i++) {
			if (i != 1) this.expression += ", ";
			this.expression += "" + DEFAULT_VARIABLE + i;
		}
		this.expression += ")";
	}
	
	public int getN() {
		return n;
	}
	
	public int getArity() {
		return arity;
	}
	
	public int[] getOutputs() {
		return outputs;
	}
	
	private void setOutput(int[] inputs, int value) {
		if ((value < 0 || value >= n) && value != -1) {
			throw new RuntimeException("output " + value + " not possible with a domain of size " + n);
		}
		outputs[inputsToIndex(inputs)] = value;
	}
	
	public void setPermutedOutputs(int[] inputs, int value) {
		for (int[] permutation : permutations(inputs)) {
			setOutput(permutation, value);
		}
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getExpression() {
		return expression;
	}
	
	public void setExpression(String expression) {
		this.expression = expression;
	}
	
	private int inputsToIndex(int... inputs) {
		int sum = 0;
		if (inputs.length != arity) {
			throw new RuntimeException(inputs.length + " inputs cannot be passed to a " + arity + "-ary operation");
		}
		for (int i = 0; i < inputs.length; i++) {
			sum += inputs[i] * (int) Math.pow(n, i);
		}
		return sum;
	}
	
	public int evaluate(int... inputs) {
		return outputs[inputsToIndex(inputs)];
	}
	
	public int[] evaluate(int[]... inputs) {
		if (inputs.length != arity) {
			throw new RuntimeException(inputs.length + " inputs cannot be passed to a " + arity + "-ary operation");
		}
		for (int i = 1; i < inputs.length; i++) {
			if (inputs[0].length != inputs[i].length) {
				throw new RuntimeException("inputs arrays must have the same size");
			}
		}
		int outputArray[] = new int[inputs[0].length];
		for (int i = 0; i < inputs[0].length; i++) {
			int[] currentInput = new int[arity];
			for (int j = 0; j < arity; j++) {
				currentInput[j] = inputs[j][i];
			}
			outputArray[i] = evaluate(currentInput);
		}
		return outputArray;
	}
	
	public boolean idempotent() {
		for (int i = 0; i < n; i++) {
			if (outputs[i * (outputs.length - 1) / (n-1)] != i) return false;
		}
		return true;
	}
	
	public boolean symmetric() {
		for (int i = 0; i < outputs.length; i++) {
			int cycle = i / n + (i % n) * (int) Math.pow(n, arity - 1);
			int swap = (arity >= 2) ? (i - i % (n*n) + n * (i%n) + (i%(n*n)) / n) : i;
			
			if (outputs[i] != outputs[cycle] || outputs[i] != outputs[swap]) return false;
		}
		return true;
	}
	
	public boolean isomorphic(Op o) {
		if (this.getN() != o.getN()) return false;
		if (this.getArity() != o.getArity()) return false;
		for (int[] perm : bijections.get(n)) {
			boolean isIsomorphic = true;
			for (int input = 0; input < outputs.length; input++) {
				int renamedInputs = 0;
				for (int digitIndex = 0; digitIndex < arity; digitIndex++) {
					renamedInputs += (int) Math.pow(n, digitIndex) * perm[(input / (int) Math.pow(n, digitIndex)) % n];
				}
				if (perm[this.getOutputs()[input]] != o.getOutputs()[renamedInputs]) {
					isIsomorphic = false;
					break;
				}
			}
			if (isIsomorphic == true) return true;
		}
		return false;
	}
	
	// can it generate another binary operation?
	public boolean unique() {
		if (arity != 2) {
			throw new RuntimeException("must be called on binary operations");
		}
		ArrayList<Op> ops = new ArrayList<Op>();
		ops.add(projection(2, 1));
		ops.add(projection(2, 2));
		while (true) {
			ArrayList<Op> toAdd = new ArrayList<Op>();
			for (Op o1 : ops) {
				for (Op o2 : ops) {
					Op composed = this.compose(o1, o2);
					boolean existsAlready = false;
					for (Op o : ops) {
						if (o.equals(composed)) existsAlready = true;
					}
					for (Op o : toAdd) {
						if (o.equals(composed)) existsAlready = true;
					}
					if (existsAlready == true) continue;
					toAdd.add(composed);
					if (composed.symmetric() && composed.equals(this) == false) {
						return false;
					}
				}
			}
			if (toAdd.size() == 0) {
				return true;
			}
			ops.addAll(toAdd);
		}
		
	}
	
	// does # of 1's exceed # of 0's, etc
	public boolean sorted() {
		for (int i = 1; i < n; i++) {
			if (count(i-1) < count(i)) return false;
		}
		return true;
	}
	
	public int count(int n) {
		int counter = 0;
		for (int k : outputs) {
			if (k == n) {
				counter++;
			}
		}
		return counter;
	}
	
	public String identify() {
		for (Op o : namedAlgebras) {
			if (o.isomorphic(this)) {
				return o.getName();
			}
		}
		return "unnamed";
	}
	
	public boolean initialized() {
		for (int i : outputs) {
			if (i == -1) return false;
		}
		return true;
	}
	
	public boolean isSubalgebra(int[] subalgebra) {
		ArrayList<Integer> arrayList = new ArrayList<Integer>();
		for (int d : subalgebra) {
			arrayList.add(d);
		}
		return isSubalgebra(arrayList);
	}
	
	private boolean isSubalgebra(ArrayList<Integer> subalgebra) {
		for (int tupleIndex = 0; tupleIndex < (int) Math.pow(subalgebra.size(), arity); tupleIndex++) {
			int[] inputs = new int[arity];
			for (int inputIndex = 0; inputIndex < inputs.length; inputIndex++) {
				inputs[inputIndex] = subalgebra.get((tupleIndex / (int) Math.pow(subalgebra.size(), inputIndex)) % subalgebra.size());
			}
			if (subalgebra.indexOf(evaluate(inputs)) == -1) {
				return false;
			}
		}
		return true;
	}
	
	public ArrayList<int[]> subalgebras() {
		if (subalgebraList == null) {
			subalgebraList = new ArrayList<int[]>();
			for (int subsetIndex = 0; subsetIndex < (int) Math.pow(2, n); subsetIndex++) {
				ArrayList<Integer> subset = new ArrayList();
				for (int digitIndex = 0; digitIndex < n; digitIndex++) {
					if ((subsetIndex / (int) Math.pow(2, n - digitIndex - 1)) % 2 == 1) {
						subset.add(digitIndex);
					}
				}
				if (subset.size() == 0) continue;
				if (isSubalgebra(subset)) {
					int[] subsetAsArray = new int[subset.size()];
					for (int i = 0; i < subset.size(); i++) {
						subsetAsArray[i] = subset.get(i);
					}
					subalgebraList.add(subsetAsArray);
				}
			}
		}
		return subalgebraList;
	}
	
	public ArrayList<int[]> subalgebras(int size) {
		ArrayList<int[]> subalgebraSubset = new ArrayList<int[]>();
		for (int[] subalgebra : subalgebras()) {
			if (subalgebra.length == size) {
				subalgebraSubset.add(subalgebra);
			}
		}
		return subalgebraSubset;
	}
	
	private Op createSubalgebra(int[] subalgebra) {
		int[] inputs = new int[(int) Math.pow(subalgebra.length, arity)];
		for (int inputIndex = 0; inputIndex < inputs.length; inputIndex++) {
			int[] input = new int[arity];
			for (int i = 0; i < input.length; i++) {
				input[i] = subalgebra[(inputIndex / (int) Math.pow(subalgebra.length, arity - i - 1)) % subalgebra.length];
			}
			int output = evaluate(input);
			int indexOfOutput = -1;
			for (int i = 0; i < subalgebra.length; i++) {
				if (subalgebra[i] == output) {
					indexOfOutput = i;
					break;
				}
			}
			if (output == -1) {
				throw new RuntimeException(Arrays.toString(subalgebra) + " not a subalgebra of " + name);
			}
			inputs[inputIndex] = indexOfOutput;
		}
		return new Op(inputs, subalgebra.length);
	}
	
	public ArrayList<Op> createSubalgebras() {
		ArrayList<Op> operations = new ArrayList<Op>();
		for (int[] subalgebra : subalgebras()) {
			operations.add(createSubalgebra(subalgebra));
		}
		return operations;
	}
	
	public ArrayList<Op> createSubalgebras(int size) {
		ArrayList<Op> operations = new ArrayList<Op>();
		for (int[] subalgebra : subalgebras(size)) {
			operations.add(createSubalgebra(subalgebra));
		}
		return operations;
	}
	
	// checks for known subalgebra
	public boolean contains(String name) {
		Op alg = null;
		for (Op o : namedAlgebras) {
			if (o.getName().equals(name)) {
				alg = o;
			}
		}
		if (alg == null) {
			throw new RuntimeException("Could not find operation " + name);
		}
		for (Op subalgebra : createSubalgebras()) {
			if (subalgebra.isomorphic(alg)) {
				return true;
			}
		}
		return false;
	}
	
	public Op compose(Op... ops) {
		if (ops.length != arity) {
			throw new RuntimeException(ops.length + " operations cannot be composed with a " + arity + "-ary operation.");
		}
		int composedArity = ops[0].getArity();
		for (Op op : ops) {
			if (op.getN() != n) {
				throw new RuntimeException("Composed operations must be over a common domain.");
			}
			if (op.getArity() != composedArity) {
				throw new RuntimeException("Composed operations must all have the same arity.");
			}
		}
		int[] outputs = new int[(int) Math.pow(n, composedArity)];
		for (int inputIndex = 0; inputIndex < (int) Math.pow(n, composedArity); inputIndex++) {
			int[] inputs = new int[arity];
			for (int i = 0; i < arity; i++) {
				inputs[i] = ops[i].getOutputs()[inputIndex];
				if (inputs[i] < 0 || inputs[i] >= n) {
					throw new RuntimeException("Cannot compose operations with uninitialized outputs");
				}
			}
			outputs[inputIndex] = evaluate(inputs);
		}
		return new Op(outputs);
	}
	
	public Op compose(String s) {
		return compose(s, new Op[] {this});
	}
	
	public int[] convolute(int[] numbers) {
		return convolute(numbers, 1);
	}
	
	public int[] convolute(int[] numbers, int iterations) {
		if (iterations == 0) return numbers;
		if (numbers.length != arity+1) {
			throw new RuntimeException(name + " cannot convolute " + numbers.length + " numbers");
		}
		int[] result = new int[numbers.length];
		for (int index = 0; index < numbers.length; index++) {
			int inputs[] = new int[arity];
			for (int i = 0; i < inputs.length; i++) {
				inputs[i] = numbers[i + (i < index ? 0 : 1)];
			}
			result[index] = evaluate(inputs);
		}
		return convolute(result, iterations-1);
	}
	
	public Op convolute(Op o) {
		if (o.getArity()+1 != arity) {
			throw new RuntimeException(name + " cannot convolute an arity " + o.getArity() + " operation");
		}
		int[] outputs = new int[(int) Math.pow(n, arity)];
		for (int inputIndex = 0; inputIndex < (int) Math.pow(n, arity); inputIndex++) {
			int[] input = new int[arity];
			for (int i = 0; i < arity; i++) {
				input[i] = (inputIndex / (int) Math.pow(n, arity - i - 1)) % n;
			}
			outputs[inputIndex] = evaluate(o.convolute(input));
		}
		return new Op(outputs);
	}
	
	public Op nextOperation() {
		if (symmetric() == false) {
			throw new RuntimeException("nextOperation can only be called on symmetric operations");
		}
		int[] inputs = new int[(int) Math.pow(n, arity+1)];
		for (int i = 0; i < inputs.length; i++) {
			inputs[i] = -1;
		}
		Op o = new Op(inputs);
		for (int d = 0; d < n; d++) {
			o.setOutput(repeat(d, arity+1), d);
		}
		for (int counter = 0; counter < CONVOLUTION_THRESHOLD; counter++) {
			o = o.convolute(this);
		}
		return o;
	}
	
	public ArrayList<int[]> uninitializedInputs() {
		ArrayList<int[]> uninitializedInputList = new ArrayList<int[]>();
		boolean isSymmetric = symmetric();
		for (int inputIndex = 0; inputIndex < (int) Math.pow(n, arity); inputIndex++) {
			if (outputs[inputIndex] == -1) {
				int[] input = new int[arity];
				for (int i = 0; i < arity; i++) {
					input[i] = (inputIndex / (int) Math.pow(n, arity - i - 1)) % n;
				}
				if (isSymmetric) {
					boolean inOrder = true;
					for (int i = 1; i < input.length; i++) {
						if (input[i-1] > input[i]) {
							inOrder = false;
							break;
						}
					}
					if (inOrder) {
						uninitializedInputList.add(input);
					}
				} else {
					uninitializedInputList.add(input);
				}
			}
		}
		return uninitializedInputList;
	}
	
	public ArrayList<Op> possibleNextOperations() {
		if (symmetric() == false || arity != 2) {
			throw new RuntimeException("possible next operations can only be computed on binary symmetric operations");
		}
		Op base = nextOperation();
		ArrayList<int[]> unknowns = new ArrayList<int[]>();
		for (int[] inputs : base.uninitializedInputs()) {
			if (isSubalgebra(inputs) && createSubalgebra(inputs).identify().equals("SGN")) {
				if (evaluate(inputs[1], inputs[2]) == inputs[0]) {
					base.setPermutedOutputs(inputs, inputs[0]);
				}
				if (evaluate(inputs[2], inputs[0]) == inputs[1]) {
					base.setPermutedOutputs(inputs, inputs[1]);
				}
				if (evaluate(inputs[0], inputs[1]) == inputs[2]) {
					base.setPermutedOutputs(inputs, inputs[2]);
				}
			} else {
				unknowns.add(inputs);
			}
		}
		ArrayList<Op> ops = new ArrayList<Op>();
		for (int tupleIndex = 0; tupleIndex < (int) Math.pow(n, unknowns.size()); tupleIndex++) {
			Op o = base.copy();
			for (int i = 0; i < unknowns.size(); i++) {
				o.setPermutedOutputs(unknowns.get(i), (tupleIndex / (int) Math.pow(n, unknowns.size()-i-1)) % n);
			}
			for (int counter = 0; counter < 4; counter++) {
				o = o.convolute(this);
			}
			ops.add(o);
		}
		// removes duplicates
		for (int index = ops.size()-1; index >= 0; index--) {
			for (int i = 0; i < index; i++) {
				if (ops.get(i).equals(ops.get(index))) {
					ops.remove(index);
					break;
				}
			}
		}
		return ops;
	}
	
	public Op copy() {
		int[] newOutputs = outputs.clone();
		Op o = new Op(newOutputs, n);
		o.setName(name);
		o.setExpression(expression);
		o.subalgebraList = new ArrayList<int[]>();
		return o;
	}
	
	public String createTable() {
		String s = "";
		for (int rowNum = 1; rowNum <= (arity+1) / 2; rowNum++) {
			String line = " ".repeat(arity / 2) + "|";
			for (int col = 0; col < (int) Math.pow(n, (arity+1) / 2); col++) {
				line += (col / (int) Math.pow(n, (arity+1) / 2 - rowNum)) % n;
				for (int spacing = 1; spacing < (arity+1) / 2; spacing++) {
					if ((col+1) % (int) Math.pow(n, spacing) == 0 && (col+1) != (int) Math.pow(n, (arity+1) / 2)) {
						line += " ";
					}
				}
			}
			s += line + "\n";
		}
		s += "-".repeat(arity / 2) + "+" + "-".repeat(((int) Math.pow(n, (arity+1) / 2 + 1) - 1) / (n-1) - (arity+1) / 2) + "\n";
		for (int row = 0; row < (int) Math.pow(n, arity / 2); row++) {
			String line = "";
			for (int colNum = 1; colNum <= arity / 2; colNum++) {
				line += (row / (int) Math.pow(n, arity / 2 - colNum)) % n;
			}
			line += "|";
			for (int col = 0; col < (int) Math.pow(n, (arity+1) / 2); col++) {
				int[] inputs = new int[arity];
				int currentOutput = outputs[row * (int) Math.pow(n, (arity+1) / 2) + col];
				if (currentOutput == -1) {
					line += "*";
				} else {
					line += currentOutput;
				}
				for (int spacing = 1; spacing < (arity+1) / 2; spacing++) {
					if ((col+1) % (int) Math.pow(n, spacing) == 0) line += " ";
				}
			}
			s += line + "\n";
			for (int spacing = 1; spacing < arity / 2; spacing++) {
				if ((row+1) % (int) Math.pow(n, spacing) == 0 && (row+1) != (int) Math.pow(n, arity / 2)) {
					s += " ".repeat(arity / 2) + "|" + "\n";
				}
			}
		}
		return s.substring(0, s.length() - 1);
	}
	
	@Override
	public String toString() {
		if (getExpression() == null) return createTable();
		else return getExpression() + ":\n" + createTable();
	}
	
	@Override
	public boolean equals(Object other) {
        if (other instanceof Op == false) return false; 
        Op o = (Op) other;
		if (this.getN() != o.getN()) return false;
		if (this.getArity() != o.getArity()) return false;
		for (int i = 0; i < outputs.length; i++) {
			if (this.getOutputs()[i] != o.getOutputs()[i]) return false;
		}
		return true; 
	}
}