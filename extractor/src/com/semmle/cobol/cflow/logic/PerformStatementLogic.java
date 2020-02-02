package com.semmle.cobol.cflow.logic;

import static com.semmle.cobol.util.Common.COMPILER_STATEMENT;
import static com.semmle.cobol.util.Common.NESTED_STATEMENTS;
import static com.semmle.cobol.util.Common.STATEMENT;

import java.util.LinkedList;
import java.util.List;

import com.semmle.cobol.cflow.Wiring;
import com.semmle.cobol.generator.effects.CFlowNode;
import com.semmle.cobol.generator.tuples.Tuple;

public class PerformStatementLogic extends EmbeddedStatementLogic {

	public PerformStatementLogic(Wiring wiring) {
		super(wiring);
	}

	/**
	 * An inline <code>PERFORM</code> statement's successor are:
	 * <ul>
	 * <li>Its first nested statement.</li>
	 * <li>If there are no nested statements, or if there is any form of
	 * repetition on the statement ( <code>VARYING</code>, <code>TIMES</code> or
	 * <code>UNTIL</code>), then we also need to include its continuation, as
	 * the body of the <code>PERFORM</code> may be skipped entirely.</li>
	 * </ul>
	 * 
	 * An out-of-line <code>PERFORM</code> statement's successors are:
	 * <ul>
	 * <li>The first procedure being named. <b>This will be resolved in
	 * QL.</b></li>
	 * <li>The continuation of the <code>PERFORM</code> statement.</li>
	 * </ul>
	 */
	@Override
	public List<CFlowNode> getSuccessors(CFlowNode n) {
		final Tuple tuple = n.getTuple();
		if ("perform_inline".equals(tuple.getName())) {
			// Inline PERFORM.
			final List<CFlowNode> successors = new LinkedList<>();

			final CFlowNode firstNestedStatement = n.get(NESTED_STATEMENTS)
					.first(STATEMENT, COMPILER_STATEMENT);

			if (!firstNestedStatement.isNull())
				successors.add(firstNestedStatement);

			if (firstNestedStatement.isNull() || isLooping(tuple))
				successors.addAll(super.getContinuations(n));

			return successors;

		} else {
			// Out-of-line PERFORM.
			return super.getContinuations(n);
		}
	}

	/**
	 * This will only get called by children of this node. In which case our
	 * answer depends on whether or not there is any form of repetition on this
	 * statement (<code>VARYING</code>, <code>TIMES</code> or <code>UNTIL</code>
	 * ). If there is, then our answer should point back to the (wrapper of the)
	 * <code>PERFORM</code> statement itself, so that the loop is formed. If
	 * there is not then it reverts back to the default behaviour.
	 */
	@Override
	public List<CFlowNode> getContinuations(CFlowNode n) {
		if (isLooping(n.getTuple()))
			return n.getParent().asList();
		else
			return super.getContinuations(n);
	}

	private boolean isLooping(Tuple t) {
		return t.hasValue("loop_form");
	}
}
