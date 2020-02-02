package com.semmle.cobol.cflow.logic;

import java.util.List;

import com.semmle.cobol.cflow.Logic;
import com.semmle.cobol.cflow.Wiring;
import com.semmle.cobol.generator.effects.CFlowNode;
import com.semmle.util.exception.CatastrophicError;

import koopa.core.data.Data;

/**
 * This class dispatches the calculation of successors and continuations to the
 * correct logic implementation, based on the actual embedded statement.
 */
public class StatementLogic implements Logic {

	/**
	 * This logic is used for any statement type which has not been registered
	 * as needing specialised treatment in {@linkplain Wiring}.
	 */
	private final EmbeddedStatementLogic genericLogic;

	private final Wiring wiring;

	public StatementLogic(Wiring wiring) {
		this.wiring = wiring;
		this.genericLogic = new EmbeddedStatementLogic(wiring);
	}

	@Override
	public List<CFlowNode> getSuccessors(CFlowNode n) {
		final CFlowNode embeddedStatement = n.firstChild();
		final Data statementType = embeddedStatement.data;

		if (!wiring.hasLogic(statementType))
			return genericLogic.getSuccessors(embeddedStatement);

		final Logic specialisedLogic = wiring.getLogic(statementType);
		if (specialisedLogic == null)
			throw new CatastrophicError(
					"Missing successors for statement " + statementType);

		return specialisedLogic.getSuccessors(embeddedStatement);
	}

	@Override
	public List<CFlowNode> getContinuations(CFlowNode n) {
		final CFlowNode embeddedStatement = n.firstChild();
		final Data statementType = embeddedStatement.data;

		if (!wiring.hasLogic(statementType))
			return genericLogic.getContinuations(embeddedStatement);

		final Logic specialisedLogic = wiring.getLogic(statementType);
		if (specialisedLogic == null)
			throw new CatastrophicError(
					"Missings continuations for statement " + statementType);

		return specialisedLogic.getContinuations(embeddedStatement);
	}
}
