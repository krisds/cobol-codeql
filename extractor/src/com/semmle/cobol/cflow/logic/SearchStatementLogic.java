package com.semmle.cobol.cflow.logic;

import static com.semmle.cobol.util.Common.AT_END;
import static com.semmle.cobol.util.Common.COMPILER_STATEMENT;
import static com.semmle.cobol.util.Common.NESTED_STATEMENTS;
import static com.semmle.cobol.util.Common.STATEMENT;
import static com.semmle.cobol.util.Common.WHEN;

import java.util.LinkedList;
import java.util.List;

import com.semmle.cobol.cflow.Wiring;
import com.semmle.cobol.generator.effects.CFlowNode;

public class SearchStatementLogic extends EmbeddedStatementLogic {

	public SearchStatementLogic(Wiring wiring) {
		super(wiring);
	}

	/**
	 * A <code>SEARCH</code> statement's successor are:
	 * <ul>
	 * <li>The first nested statement of any <code>WHEN</code> phrase.</li>
	 * <li>The <code>AT END</code> phrase.</li>
	 * <li>Its continuation, if none of the above are found.</li>
	 * </ul>
	 */
	@Override
	public List<CFlowNode> getSuccessors(CFlowNode n) {
		final List<CFlowNode> successors = new LinkedList<>();

		for (CFlowNode when : n.all(WHEN)) {
			successors.addAll(when.get(NESTED_STATEMENTS)
					.first(STATEMENT, COMPILER_STATEMENT).asList());
		}

		final CFlowNode atEnd = n.get(AT_END);
		if (!atEnd.isNull())
			successors.add(atEnd);
		else
			successors.addAll(getContinuations(n));

		return successors;
	}
}
