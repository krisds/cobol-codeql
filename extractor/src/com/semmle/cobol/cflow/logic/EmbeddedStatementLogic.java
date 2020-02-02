package com.semmle.cobol.cflow.logic;

import static com.semmle.cobol.util.Common.CFLOW__EXIT_NODE;
import static com.semmle.cobol.util.Common.COMPILER_STATEMENT;
import static com.semmle.cobol.util.Common.STATEMENT;

import java.util.LinkedList;
import java.util.List;

import com.semmle.cobol.cflow.Logic;
import com.semmle.cobol.cflow.Wiring;
import com.semmle.cobol.generator.effects.CFlowNode;

/**
 * Statements in the AST have a <code>statement</code> wrapper. The logic for
 * the wrapper is encoded in {@linkplain StatementLogic}. This class provides
 * the basis for implementing the logic of the nested statements.
 */
public class EmbeddedStatementLogic implements Logic {

	private final Wiring wiring;

	public EmbeddedStatementLogic(Wiring wiring) {
		this.wiring = wiring;
	}

	/**
	 * The successor of any basic statement is just its continuation, or any of
	 * its branches.
	 * <p>
	 * More specialized statements (e.g. an <code>IF</code> statement) will
	 * override this to match their own logic.
	 */
	@Override
	public List<CFlowNode> getSuccessors(CFlowNode n) {
		final List<CFlowNode> successors = new LinkedList<>();

		successors.addAll(n.all(wiring.getNodeTypesTagged(Wiring.Tag.BRANCH)));
		successors.addAll(getContinuations(n));

		return successors;
	}

	/**
	 * The continuation of any basic statement is:
	 * <ul>
	 * <li>The statement following this one.</li>
	 * <li>Or else, the continuation of the parent flow node.</li>
	 * </ul>
	 */
	@Override
	public List<CFlowNode> getContinuations(CFlowNode n) {
		final CFlowNode nextStatement = n.getParent().next(STATEMENT,
				COMPILER_STATEMENT, CFLOW__EXIT_NODE);

		if (!nextStatement.isNull())
			return nextStatement.asList();

		final CFlowNode parentFlowNode = n.getParent()
				.closest(wiring.getNodeTypesTagged(Wiring.Tag.CFLOW));

		return wiring.getLogic(parentFlowNode.data) //
				.getContinuations(parentFlowNode);
	}
}
