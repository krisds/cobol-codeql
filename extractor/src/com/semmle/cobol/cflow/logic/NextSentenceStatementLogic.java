package com.semmle.cobol.cflow.logic;

import static com.semmle.cobol.util.Common.SENTENCE;

import java.util.List;

import com.semmle.cobol.cflow.Wiring;
import com.semmle.cobol.generator.effects.CFlowNode;

public class NextSentenceStatementLogic extends EmbeddedStatementLogic {

	private final Wiring wiring;

	public NextSentenceStatementLogic(Wiring wiring) {
		super(wiring);
		this.wiring = wiring;
	}

	/**
	 * A <code>NEXT SENTENCE</code> statement's successors is its continuation.
	 */
	@Override
	public List<CFlowNode> getSuccessors(CFlowNode n) {
		return getContinuations(n);
	}

	/**
	 * The continuation of a <code>NEXT SENTENCE</code> statement is the
	 * continuation of the sentence this statement is in.
	 */
	@Override
	public List<CFlowNode> getContinuations(CFlowNode n) {
		final CFlowNode sentence = n.closest(SENTENCE);
		return wiring.getLogic(SENTENCE).getContinuations(sentence);
	}
}
