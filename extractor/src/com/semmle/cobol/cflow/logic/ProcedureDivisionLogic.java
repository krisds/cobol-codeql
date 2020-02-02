package com.semmle.cobol.cflow.logic;

import static com.semmle.cobol.util.Common.DECLARATIVE_SECTION;
import static com.semmle.cobol.util.Common.ENTRY;
import static com.semmle.cobol.util.Common.PARAGRAPH;
import static com.semmle.cobol.util.Common.SECTION;
import static com.semmle.cobol.util.Common.SENTENCE;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.semmle.cobol.cflow.Logic;
import com.semmle.cobol.cflow.Wiring;
import com.semmle.cobol.generator.effects.CFlowNode;

public class ProcedureDivisionLogic implements Logic {

	public ProcedureDivisionLogic(Wiring wiring) {
	}

	/**
	 * Successors of a procedure division are:
	 * <ul>
	 * <li>All sections in the DECLARATIVES section.</li>
	 * <li>All ENTRY statements.</li>
	 * <li>The first child which is a sentence, paragraph or section.</li>
	 * </ul>
	 */
	@Override
	public List<CFlowNode> getSuccessors(CFlowNode n) {
		final List<CFlowNode> successors = new LinkedList<>();

		successors.addAll(n.all(DECLARATIVE_SECTION));
		successors.addAll(n.findAll(ENTRY));
		successors.addAll(n.first(SENTENCE, PARAGRAPH, SECTION).asList());

		return successors;
	}

	/**
	 * A procedure division has no more continuations. So this will always
	 * return the empty list.
	 */
	@Override
	public List<CFlowNode> getContinuations(CFlowNode n) {
		return Collections.emptyList();
	}
}
