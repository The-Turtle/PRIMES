import java.util.Arrays;
import java.util.ArrayList;

class SymOp {
	public static final int n = 5;
	private static ArrayList<int[][]> inputArrays = new ArrayList<int[][]>();
	
	private static int getIndex(int[] inputs, int n) {
		return Tools.getIndex(inputs, n) - Tools.min(inputs) - 1;
	}
	
	public static int[][] allInputs(int arity) {
		for (int ar = inputArrays.size(); ar <= arity; ar++) {
			inputArrays.add(Tools.createInputArray(n, ar));
		}
		return inputArrays.get(arity);
	}
	
	public final int arity;
	private final int[] outputs;
	
	public SymOp(int arity) {
		this.arity = arity;
		this.outputs = new int[Tools.choose(n-1+arity, n-1) - n];
		for (int i = 0; i < outputs.length; i++) {
			outputs[i] = -1;
		}
	}
	
	public SymOp(int... outputs) {
		this.outputs = outputs;
		int ar = 1;
		while (true) {
			int size = Tools.choose(n-1+ar, n-1) - n;
			if (size < outputs.length) ar++;
			if (size == outputs.length) break;
			if (size > outputs.length) {
				throw new RuntimeException("cannot determine arity with " + outputs.length + " outputs over a domain of size " + n);
			}
		}
		this.arity = ar;
	}
	
	public void setOutput(int[] inputs, int output) {
		if (inputs.length != arity) {
			throw new RuntimeException("cannot set output of " + inputs.length + "-element tuple");
		}
		if (Tools.allEqual(inputs)) {
			if (inputs[0] == output) return;
			throw new RuntimeException("cannot set output of all-equal tuple");
		}
		outputs[getIndex(inputs, n)] = output;
	}
	
	public ArrayList<int[]> undefinedInputs() {
		ArrayList<int[]> inputs = new ArrayList<int[]>();
		for (int[] input : allInputs(arity)) {
			int output = evaluate(input);
			if (output < 0 || output >= n) inputs.add(input);
		}
		return inputs;
	}
	
	public boolean initialized() {
		return undefinedInputs().size() == 0;
	}
	
	public int evaluate(int... inputs) {
		if (inputs.length != arity) {
			throw new RuntimeException(inputs.length + " arguments cannot be passed to " + arity + "-ary operation");
		}
		if (Tools.allEqual(inputs)) return inputs[0];
		for (int d : inputs) {
			if (d < 0 || d >= n) {
				throw new RuntimeException(Arrays.toString(inputs) + " cannot be evaluated over a domain of size " + n);
			}
		}
		return outputs[getIndex(inputs, n)];
	}
	
	public int[] evaluate(int[]... inputs) {
		int[] outputs = new int[inputs[0].length];
		for (int[] array : inputs) {
			if (array.length != outputs.length) {
				throw new RuntimeException("input arrays must all be the same length");
			}
		}
		for (int i = 0; i < outputs.length; i++) {
			int[] input = new int[inputs.length];
			for (int j = 0; j < inputs.length; j++) {
				input[j] = inputs[j][i];
			}
			outputs[i] = evaluate(input);
		}
		return outputs;
	}
	
	public int[] curl(int... inputs) {
		if (inputs.length != arity+1) {
			throw new RuntimeException("cannot curl " + Arrays.toString(inputs) + " with arity " + arity + " operation");
		}
		int[] outputs = new int[inputs.length];
		for (int i = 0; i < inputs.length; i++) {
			int[] removed = new int[arity];
			for (int j = 0; j < arity; j++) {
				removed[j] = inputs[j + ((j >= i) ? 1 : 0)];
			}
			outputs[i] = evaluate(removed);
		}
		return outputs;
	}
	
	public boolean containsLin() {
		if (arity != 2) {
			throw new RuntimeException("linear subalgebra check can only be done on binary operations");
		}
		for (int a = 0; a < n; a++) {
			for (int b = 0; b < n; b++) {
				int c = evaluate(a, b);
				if (a == b || b == c || c == a) continue;
				if (evaluate(b, c) == a && evaluate(a, c) == b) return true;
			}
		}
		return false;
	}
	
	public String table() {
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
				for (int i = 0; i < arity; i++) {
					inputs[i] = ((row * (int) Math.pow(n, (arity+1) / 2) + col) / (int) Math.pow(n, arity - i - 1)) % n;
				}
				int currentOutput = evaluate(inputs);
				if (currentOutput < 0 || currentOutput >= n) {
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
	
	public SymOp clone() {
		return new SymOp(outputs.clone());
	}
	
	@Override
	public String toString() {
		String s = "";
		int counter = 0;
		for (int[] input : allInputs(arity)) {
			for (int d : input) {
				s += d;
			}
			s += " -> " + evaluate(input) + "\n";
		}
		return s;
	}
	
	@Override
	public boolean equals(Object other) {
        if (!(other instanceof SymOp)) { 
            return false; 
        }
        return Arrays.equals(((SymOp) other).outputs, outputs);
	}
}