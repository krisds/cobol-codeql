package com.semmle.cobol.halstead;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Supporting class which tracks tokens as being either operators or
 * operands. We keep track of distinct tokens, based on their text (but
 * ignoring case), as well as of the overall count of individual tokens.
 */
class Tokens {
	public final Set<String> operands = new LinkedHashSet<String>();
	public int operandCount = 0;

	public final Set<String> operators = new LinkedHashSet<String>();
	public int operatorCount = 0;

	public void addOperand(String text) {
		text = text.toUpperCase();
		operands.add(text);
		operandCount += 1;
	}

	public void addOperator(String text) {
		text = text.toUpperCase();
		operators.add(text);
		operatorCount += 1;
	}

	/**
	 * Add the data of another Tokens instance to this one.
	 */
	public void include(Tokens sub) {
		operands.addAll(sub.operands);
		operandCount += sub.operandCount;

		operators.addAll(sub.operators);
		operatorCount += sub.operatorCount;
	}
}
