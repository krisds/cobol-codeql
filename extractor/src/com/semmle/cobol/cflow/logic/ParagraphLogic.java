package com.semmle.cobol.cflow.logic;

import static com.semmle.cobol.util.Common.DECLARATIVE_SECTION;
import static com.semmle.cobol.util.Common.PARAGRAPH;
import static com.semmle.cobol.util.Common.PROCEDURE_DIVISION;
import static com.semmle.cobol.util.Common.SECTION;

import java.util.List;

import com.semmle.cobol.cflow.Logic;
import com.semmle.cobol.cflow.Wiring;
import com.semmle.cobol.generator.effects.CFlowNode;
import com.semmle.cobol.util.Common;

public class ParagraphLogic implements Logic {

	private final Wiring wiring;

	public ParagraphLogic(Wiring wiring) {
		this.wiring = wiring;
	}

	/**
	 * Successors of a paragraph are:
	 * <ul>
	 * <li>The first child which is a sentence.</li>
	 * <li>If there are no such children, the continuation of the
	 * paragraph.</li>
	 * </ul>
	 */
	@Override
	public List<CFlowNode> getSuccessors(CFlowNode n) {
		final CFlowNode firstSentence = n.first(Common.SENTENCE);
		if (!firstSentence.isNull())
			return firstSentence.asList();
		else
			return getContinuations(n);
	}

	/**
	 * The continuation of a paragraph is:
	 * <ul>
	 * <li>The paragraph or section immediately following the paragraph.</li>
	 * <li>Or else, the continuation of the parent flow node.</li>
	 * </ul>
	 * Where the parent flow can be any of:
	 * <ul>
	 * <li>a section</li>
	 * <li>a declarative section</li>
	 * <li>a procedure division</li>
	 * </ul>
	 */
	@Override
	public List<CFlowNode> getContinuations(CFlowNode n) {
		final CFlowNode next = n.next(PARAGRAPH, SECTION);

		if (!next.isNull())
			return next.asList();

		final CFlowNode parent = n.closest(SECTION, DECLARATIVE_SECTION,
				PROCEDURE_DIVISION);

		return wiring.getLogic(parent.data).getContinuations(parent);
	}
}
