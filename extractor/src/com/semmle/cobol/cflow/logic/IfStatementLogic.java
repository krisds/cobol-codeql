package com.semmle.cobol.cflow.logic;

import static com.semmle.cobol.util.Common.COMPILER_STATEMENT;
import static com.semmle.cobol.util.Common.ELSE;
import static com.semmle.cobol.util.Common.NESTED_STATEMENTS;
import static com.semmle.cobol.util.Common.STATEMENT;
import static com.semmle.cobol.util.Common.THEN;

import java.util.LinkedList;
import java.util.List;

import com.semmle.cobol.cflow.Wiring;
import com.semmle.cobol.generator.effects.CFlowNode;

public class IfStatementLogic extends EmbeddedStatementLogic {

	public IfStatementLogic(Wiring wiring) {
		super(wiring);
	}

	/**
	 * An <code>IF</code> statement's successor are:
	 * <ul>
	 * <li>The first statement in its <code>THEN</code> branch.</li>
	 * <li>The first statement in its <code>ELSE</code> branch.</li>
	 * <li>Its continuation, if there is no <code>ELSE</code> branch.</li>
	 * </ul>
	 */
	@Override
	public List<CFlowNode> getSuccessors(CFlowNode n) {
		final List<CFlowNode> successors = new LinkedList<>();

		final CFlowNode thenFirstStatement = n.get(THEN).get(NESTED_STATEMENTS)
				.first(STATEMENT, COMPILER_STATEMENT);

		if (!thenFirstStatement.isNull())
			successors.add(thenFirstStatement);

		final CFlowNode elseBranch = n.get(ELSE);
		if (!elseBranch.isNull()) {
			final CFlowNode elseFirstStatement = elseBranch
					.get(NESTED_STATEMENTS)
					.first(STATEMENT, COMPILER_STATEMENT);

			if (!elseFirstStatement.isNull())
				successors.add(elseFirstStatement);

		} else
			successors.addAll(getContinuations(n));

		return successors;
	}
}
