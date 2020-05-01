import java.util.ArrayList;

class Clone implements java.io.Serializable {
	private static final int MAX_ARITY = Op.MAX_ARITY;
	private static int maxOperations = Integer.MAX_VALUE;
	private static boolean printSymmetric = false;
	private static boolean printCounter = false;
	
	private Op[] generators;
	private ArrayList<ArrayList<Op>> operations = new ArrayList<ArrayList<Op>>();
	
	public static void setMaxOperations(int maxOperations) {
		Clone.maxOperations = maxOperations;
	}
	
	public static void setPrintSymmetric() {
		printSymmetric = true;
	}
	
	public static void setPrintCounter() {
		printCounter = true;
	}
	
	public Clone(Op... generators) {
		if (maxOperations == 0) {
			throw new RuntimeException("maxOperations not initialized");
		}
		this.generators = generators;
		for (int ar = 0; ar <= MAX_ARITY; ar++) {
			operations.add(new ArrayList<Op>());
		}
	}
	
	public void generateOperations(int arity) {
		ArrayList<Op> ops = operations.get(arity);
		if (ops.size() != 0) return;
		for (int i = 1; i <= arity; i++) {
			ops.add(Op.projection(arity, i));
		}
		int previousIterationLimit = 0;
		outer:
		while (true) {
			ArrayList<Op> newOps = new ArrayList<Op>();
			for (Op outerOp : generators) {
				for (int tupleIndex = 0; tupleIndex < (int) Math.pow(ops.size(), outerOp.getArity()); tupleIndex++) {
					Op[] tuple = new Op[outerOp.getArity()];
					boolean alreadyComputed = true;
					for (int i = 0; i < outerOp.getArity(); i++) {
						int opIndex = (tupleIndex / (int) Math.pow(ops.size(), outerOp.getArity() - i - 1)) % ops.size();
						if (opIndex >= previousIterationLimit) alreadyComputed = false;
						tuple[i] = ops.get(opIndex);
					}
					if (alreadyComputed == true) continue;
					Op newOp = outerOp.compose(tuple);
					String s = outerOp.getName();
					s += "(";
					for (int i = 0; i < tuple.length; i++) {
						if (i != 0) s += ", ";
						s += tuple[i].getExpression();
					}
					s += ")";
					boolean alreadyExists = false;
					for (Op o : ops) {
						if (newOp.equals(o)) {
							if (o.getExpression().length() > s.length()) {
								o.setExpression(s);
							}
							alreadyExists = true;
							break;
						}
					}
					if (alreadyExists == true) continue;
					for (Op o : newOps) {
						if (newOp.equals(o)) {
							if (o.getExpression().length() > s.length()) {
								o.setExpression(s);
							}
							alreadyExists = true;
							break;
						}
					}
					if (alreadyExists == true) continue;
					if (printCounter) System.out.println(ops.size() + newOps.size());
					newOp.setExpression(s);
					newOps.add(newOp);
					if (printSymmetric == true && newOp.symmetric()) {
						System.out.println(newOp);
					}
					if (ops.size() + newOps.size() >= maxOperations) {
						ops.addAll(newOps);
						System.out.println("WARNING: CLONE NOT COMPLETED");
						break outer;
					}
				}
			}
			if (newOps.size() == 0) break outer;
			previousIterationLimit = ops.size();
			ops.addAll(newOps);
		}
	}
	
	public ArrayList<Op> getOperations(int arity) {
		generateOperations(arity);
		return operations.get(arity);
	}
	
	public ArrayList<Op> symmetricOperations(int arity) {
		generateOperations(arity);
		ArrayList<Op> symOps = new ArrayList<Op>();
		for (Op o : operations.get(arity)) {
			if (o.symmetric()) symOps.add(o);
		}
		return symOps;
	}
}