package com.semmle.cobol.cflow.logic;

import static com.semmle.cobol.util.Common.COMPILER_STATEMENT;
import static com.semmle.cobol.util.Common.NESTED_STATEMENTS;
import static com.semmle.cobol.util.Common.STATEMENT;
import static com.semmle.cobol.util.Common.WHEN;
import static com.semmle.cobol.util.Common.WHEN_OTHER;

import java.util.LinkedList;
import java.util.List;

import com.semmle.cobol.cflow.Wiring;
import com.semmle.cobol.generator.effects.CFlowNode;

public class EvaluateStatementLogic extends EmbeddedStatementLogic {

	public EvaluateStatementLogic(Wiring wiring) {
		super(wiring);
	}

	/**
	 * An <code>EVALUATE</code> statement's successor are:
	 * <ul>
	 * <li>The first statement in any <code>WHEN</code> branch.</li>
	 * <li>The first statement in the <code>WHEN OTHER</code> branch.</li>
	 * <li>Its continuation, if there is no <code>WHEN OTHER</code> branch.</li>
	 * </ul>
	 */
	@Override
	public List<CFlowNode> getSuccessors(CFlowNode n) {
		final List<CFlowNode> successors = new LinkedList<>();

		for (CFlowNode when : n.all(WHEN)) {
			final CFlowNode whenFirstStatement = when.get(NESTED_STATEMENTS)
					.first(STATEMENT, COMPILER_STATEMENT);

			if (!whenFirstStatement.isNull())
				successors.add(whenFirstStatement);
		}

		final CFlowNode whenOther = n.get(WHEN_OTHER);

		if (!whenOther.isNull()) {
			final CFlowNode whenOtherFirstStatement = whenOther
					.get(NESTED_STATEMENTS)
					.first(STATEMENT, COMPILER_STATEMENT);

			if (!whenOtherFirstStatement.isNull())
				successors.add(whenOtherFirstStatement);

		} else
			successors.addAll(getContinuations(n));

		return successors;
	}
}
