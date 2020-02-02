package com.semmle.cobol.cflow.logic;

import static com.semmle.cobol.util.Common.COMPILER_STATEMENT;
import static com.semmle.cobol.util.Common.NESTED_STATEMENTS;
import static com.semmle.cobol.util.Common.STATEMENT;

import java.util.List;

import com.semmle.cobol.cflow.Logic;
import com.semmle.cobol.cflow.Wiring;
import com.semmle.cobol.generator.effects.CFlowNode;

/**
 * This class is meant to handle control flow logic for all types of branches
 * which are used as exception handlers. E.g. <code>ON EXCEPTION</code>,
 * <code>ON OVERFLOW</code>, <code>AT END</code>.
 */
public class BranchLogic implements Logic {

	private final Wiring wiring;

	public BranchLogic(Wiring wiring) {
		this.wiring = wiring;
	}

	/**
	 * Successors of a branch are:
	 * <ul>
	 * <li>The first nested statement.</li>
	 * <li>If there are no such children, the continuation of the branch.</li>
	 * </ul>
	 */
	@Override
	public List<CFlowNode> getSuccessors(CFlowNode n) {
		final CFlowNode firstStatement = n.get(NESTED_STATEMENTS)
				.first(STATEMENT, COMPILER_STATEMENT);

		if (!firstStatement.isNull())
			return firstStatement.asList();
		else
			return getContinuations(n);
	}

	/**
	 * The continuation of a branch is:
	 * <ul>
	 * <li>The continuation of the parent flow node.</li>
	 * </ul>
	 * Where the parent flow can be any of:
	 * <ul>
	 * <li>a statement</li>
	 * </ul>
	 */
	@Override
	public List<CFlowNode> getContinuations(CFlowNode n) {
		final CFlowNode parentFlowNode = n.closest(STATEMENT);
		return wiring.getLogic(STATEMENT).getContinuations(parentFlowNode);
	}
}
