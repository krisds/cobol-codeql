package com.semmle.cobol.cflow.logic;

import java.util.Collections;
import java.util.List;

import com.semmle.cobol.cflow.Wiring;
import com.semmle.cobol.generator.effects.CFlowNode;
import com.semmle.cobol.generator.tuples.Tuple;

public class GoToStatementLogic extends EmbeddedStatementLogic {

	public GoToStatementLogic(Wiring wiring) {
		super(wiring);
	}

	/**
	 * A GO TO statement's successor are:
	 * <ul>
	 * <li>All units being named as targets. <b>These will be resolved in
	 * QL.</b></li>
	 * <li>In case there are no targets, we assume there exists an ALTER
	 * statement to add them, but allow for the possibility that it acts as a
	 * <code>CONTINUE</code> while it remains unaltered. <b>Targets provided
	 * through an <code>ALTER</code> statement will be resolved in QL.</b></li>
	 * <li>In case of a DEPENDING ON, the continuation of the statement.</li>
	 * </ul>
	 */
	@Override
	public List<CFlowNode> getSuccessors(CFlowNode n) {
		final Tuple tuple = n.getTuple();

		// DEPENDING ON ?
		if (tuple.hasValue("depending_on"))
			return getContinuations(n);

		// No targets ? Assuming ALTER.
		if (!tuple.hasValue("targets"))
			return getContinuations(n);

		// Base case.
		return Collections.emptyList();
	}
}
