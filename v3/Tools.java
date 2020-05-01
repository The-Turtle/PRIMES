import java.util.Arrays;
import java.util.ArrayList;

final class Tools {
	public static int choose(int n, int k) {
		if (k < 0 || k > n) return 0;
		if (2*k > n) return choose(n, n-k);
		int product = 1;
		for (int i = 1; i <= k; i++) {
			product *= n-i+1;
			product /= i;
		}
		return product;
	}
	
	public static int min(int[] array) {
		int min = array[0];
		for (int n : array) {
			min = Math.min(n, min);
		}
		return min;
	}
	
	public static int max(int[] array) {
		int max = array[0];
		for (int n : array) {
			max = Math.max(n, max);
		}
		return max;
	}
	
	public static boolean allEqual(int... inputs) {
		for (int i = 1; i < inputs.length; i++) {
			if (inputs[i-1] != inputs[i]) return false;
		}
		return true;
	}
	
	public static int[] repeat(int n, int times) {
		int[] array = new int[times];
		for (int i = 0; i < array.length; i++) {
			array[i] = n;
		}
		return array;
	}
	
	public static int count(int[] array, int target) {
		int counter = 0;
		for (int n : array) {
			if (n == target) counter++;
		}
		return counter;
	}
	
	public static int[][] cartesianPower(int[] array, int exponent) {
		int[][] result = new int[(int) Math.pow(array.length, exponent)][exponent];
		for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < exponent; j++) {
				result[i][j] = array[(i / (int) Math.pow(array.length, exponent - j - 1)) % array.length];
			}
		}
		return result;
	}
	
	public static int[] createDomain(int n) {
		int[] domain = new int[n];
		for (int i = 0; i < n; i++) {
			domain[i] = i;
		}
		return domain;
	}
	
	public static int[][] createInputArray(int n, int arity) {
		int[][] inputList = new int[choose(n-1+arity, n-1)][arity];
		int[] currentTuple = new int[arity];
		for (int index = 1; index < inputList.length; index++) {
			int lastNonmaximalIndex = currentTuple.length-1;
			while (true) {
				if (currentTuple[lastNonmaximalIndex] != n-1) break;
				lastNonmaximalIndex--;
			}
			currentTuple[lastNonmaximalIndex]++;
			for (int i = lastNonmaximalIndex+1; i < currentTuple.length; i++) {
				currentTuple[i] = currentTuple[lastNonmaximalIndex];
			}
			inputList[index] = currentTuple.clone();
		}
		return inputList;
	}
	
	public static int getIndex(int[] inputs, int n) {
		int[] sorted = inputs.clone();
		Arrays.sort(sorted);
		int index = 0;
		for (int i = 0; i < sorted.length; i++) {
			index += choose(n+sorted.length-i-sorted[i]-2, sorted.length-i);
		}
		return choose(n-1+sorted.length, n-1) - index - 1;
	}
	
	public static int distinctArrays(ArrayList<int[]> arrays) {
		int count = 0;
		for (int i = 0; i < arrays.size(); i++) {
			boolean present = false;
			for (int j = 0; j < i; j++) {
				if (Arrays.equals(arrays.get(j), arrays.get(i))) {
					present = true;
					break;
				}
			}
			if (!present) count++;
		}
		return count;
	}
}