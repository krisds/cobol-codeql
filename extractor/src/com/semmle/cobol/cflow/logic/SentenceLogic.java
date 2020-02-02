package com.semmle.cobol.cflow.logic;

import static com.semmle.cobol.util.Common.CFLOW__EXIT_NODE;
import static com.semmle.cobol.util.Common.COMPILER_STATEMENT;
import static com.semmle.cobol.util.Common.DECLARATIVE_SECTION;
import static com.semmle.cobol.util.Common.PARAGRAPH;
import static com.semmle.cobol.util.Common.PROCEDURE_DIVISION;
import static com.semmle.cobol.util.Common.SECTION;
import static com.semmle.cobol.util.Common.SENTENCE;
import static com.semmle.cobol.util.Common.STATEMENT;

import java.util.List;

import com.semmle.cobol.cflow.Logic;
import com.semmle.cobol.cflow.Wiring;
import com.semmle.cobol.generator.effects.CFlowNode;

public class SentenceLogic implements Logic {

	public SentenceLogic(Wiring wiring) {
	}

	/**
	 * Successors of a sentence are:
	 * <ul>
	 * <li>The first child which is a statement.</li>
	 * <li>If there are no such children, the continuation of the sentence.</li>
	 * </ul>
	 */
	@Override
	public List<CFlowNode> getSuccessors(CFlowNode n) {
		final CFlowNode firstChild = n.first(STATEMENT, COMPILER_STATEMENT);
		if (!firstChild.isNull())
			return firstChild.asList();
		else
			return getContinuations(n);
	}

	/**
	 * The continuation of a sentence is:
	 * <ul>
	 * <li>The sentence, paragraph or section immediately following this
	 * one</li>
	 * <li>Or else, the closest following exit node.</li>
	 * </ul>
	 */
	@Override
	public List<CFlowNode> getContinuations(CFlowNode n) {
		final CFlowNode nextUnit = n.next(SENTENCE, PARAGRAPH, SECTION);
		if (!nextUnit.isNull())
			return nextUnit.asList();

		final CFlowNode proc = n.closest(PARAGRAPH, SECTION,
				DECLARATIVE_SECTION, PROCEDURE_DIVISION);

		return proc.findLast(CFLOW__EXIT_NODE).asList();
	}
}
