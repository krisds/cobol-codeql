package com.semmle.cobol.cflow.logic;

import java.util.Collections;
import java.util.List;

import com.semmle.cobol.cflow.Wiring;
import com.semmle.cobol.generator.effects.CFlowNode;
import com.semmle.cobol.generator.tuples.Tuple;

public class StopStatementLogic extends EmbeddedStatementLogic {

	public StopStatementLogic(Wiring wiring) {
		super(wiring);
	}

	/**
	 * A <code>STOP RUN</code> basically ceases execution of the program, and
	 * returns control to the operating system. Except for the following...
	 * <p>
	 * From Micro Focus documentation: "The <code>STOP ITERATOR</code> statement
	 * has the effect of terminating the iterator." An iterator is built as a
	 * source unit with its own procedure division, and so a "
	 * <code>STOP ITERATOR</code>" would mean no more successors in that
	 * procedure division.
	 * <p>
	 * From Micro Focus documentation: "If <code>STOP literal-1</code> is
	 * specified, the execution of the run unit is suspended and
	 * <code>literal-1</code> is communicated to the operator. Continuation of
	 * the execution of the run unit begins with the next executable statement
	 * when the operator presses the ENTER key or its equivalent."
	 */
	@Override
	public List<CFlowNode> getSuccessors(CFlowNode n) {
		final Tuple tuple = n.getTuple();
		if (!tuple.hasValue("literal"))
			return Collections.emptyList();
		else
			return getContinuations(n);
	}
}
