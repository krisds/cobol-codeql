package com.semmle.cobol.cflow.logic;

import static com.semmle.cobol.util.Common.SENTENCE;

import java.util.Collections;
import java.util.List;

import com.semmle.cobol.cflow.Logic;
import com.semmle.cobol.cflow.Wiring;
import com.semmle.cobol.generator.effects.CFlowNode;

public class DeclarativeSectionLogic implements Logic {

	public DeclarativeSectionLogic(Wiring wiring) {
	}

	/**
	 * Successors of a <code>DECLARATIVE</code> section are:
	 * <ul>
	 * <li>The first child, which is a sentence holding a <code>USE</code>
	 * statement.</li>
	 * </ul>
	 */
	@Override
	public List<CFlowNode> getSuccessors(CFlowNode n) {
		return n.first(SENTENCE).asList();
	}

	/**
	 * A <code>DECLARATIVE</code> section has no continuation.
	 */
	@Override
	public List<CFlowNode> getContinuations(CFlowNode n) {
		return Collections.emptyList();
	}
}
