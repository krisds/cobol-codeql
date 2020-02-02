package com.semmle.cobol.cflow.logic;

import static com.semmle.cobol.util.Common.PARAGRAPH;
import static com.semmle.cobol.util.Common.PROCEDURE_DIVISION;
import static com.semmle.cobol.util.Common.SECTION;
import static com.semmle.cobol.util.Common.SENTENCE;

import java.util.List;

import com.semmle.cobol.cflow.Logic;
import com.semmle.cobol.cflow.Wiring;
import com.semmle.cobol.generator.effects.CFlowNode;

public class SectionLogic implements Logic {

	private final Wiring wiring;

	public SectionLogic(Wiring wiring) {
		this.wiring = wiring;
	}

	/**
	 * Successors of a section are:
	 * <ul>
	 * <li>The first child which is a sentence or paragraph.</li>
	 * <li>If there are no such children, the continuation of the section.</li>
	 * </ul>
	 */
	@Override
	public List<CFlowNode> getSuccessors(CFlowNode n) {
		final CFlowNode next = n.first(SENTENCE, PARAGRAPH);
		if (!next.isNull())
			return next.asList();
		else
			return getContinuations(n);
	}

	/**
	 * The continuation of a section is:
	 * <ul>
	 * <li>The section immediately following the section.</li>
	 * <li>Or else, the continuation of the parent flow node.</li>
	 * </ul>
	 * Where the parent flow can be any of:
	 * <ul>
	 * <li>a procedure division</li>
	 * </ul>
	 */
	@Override
	public List<CFlowNode> getContinuations(CFlowNode n) {
		final CFlowNode next = n.next(SECTION);
		if (!next.isNull())
			return next.asList();

		final CFlowNode parent = n.closest(PROCEDURE_DIVISION);
		return wiring.getLogic(PROCEDURE_DIVISION).getContinuations(parent);
	}
}
