package com.semmle.cobol.halstead;

import koopa.core.data.Data;

/**
 * Supporting class which holds the basic counts which make up the Halstead
 * metrics.
 */
public class HalsteadCount implements Data {
	/** Number of distinct operators. */
	public final int n1;
	/** Number of distinct operands. */
	public final int n2;
	/** Total number of operators. */
	public final int N1;
	/** Total number of operands. */
	public final int N2;

	public HalsteadCount(Tokens tokens) {
		this.N1 = tokens.operatorCount;
		this.N2 = tokens.operandCount;

		this.n1 = tokens.operators.size();
		this.n2 = tokens.operands.size();
	}

	@Override
	public String toString() {
		return "N1: " + N1 + ", N2: " + N2 + ", n1: " + n1 + ", n2: " + n2;
	}
}