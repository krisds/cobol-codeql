package com.semmle.cobol.cflow.logic;

import static com.semmle.cobol.util.Common.DECLARATIVE_SECTION;
import static com.semmle.cobol.util.Common.PARAGRAPH;
import static com.semmle.cobol.util.Common.PROCEDURE_DIVISION;
import static com.semmle.cobol.util.Common.SECTION;

import java.util.Collections;
import java.util.List;

import com.semmle.cobol.cflow.Logic;
import com.semmle.cobol.cflow.Wiring;
import com.semmle.cobol.generator.effects.CFlowNode;

import koopa.core.data.markers.Start;
import koopa.core.trees.Tree;

public class ExitNodeLogic implements Logic {

	public ExitNodeLogic(Wiring wiring) {
	}

	@Override
	public List<CFlowNode> getSuccessors(CFlowNode n) {
		return getContinuations(n);
	}

	/**
	 * The successor of an exit node is:
	 * <ul>
	 * <li>The paragraph or section immediately following the paragraph or
	 * section it belongs to.</li>
	 * <li>Or else, the continuation of the parent flow node of the paragraph or
	 * section it belongs to.</li>
	 * </ul>
	 * Where the parent flow can be any of:
	 * <ul>
	 * <li>a procedure division</li>
	 * </ul>
	 */
	@Override
	public List<CFlowNode> getContinuations(CFlowNode n) {
		// An exit node may appear in any of the following:
		CFlowNode proc = n.closest(PARAGRAPH, SECTION, DECLARATIVE_SECTION,
				PROCEDURE_DIVISION);

		// Let's try paragraphs first, as these can also be embedded in the
		// remaining options. And we may have to move up a level to one of
		// those...
		if (proc.data == PARAGRAPH) {
			// A paragraph continues to the paragraph following it, if there is
			// one.
			final CFlowNode nextParagraph = proc.next(PARAGRAPH);

			// Did we get one ?
			if (!nextParagraph.isNull())
				return nextParagraph.asList();

			// ... If not, try the paragraph's container.
			proc = n.closest(SECTION, DECLARATIVE_SECTION, PROCEDURE_DIVISION);
		}

		// Maybe it's a section instead ?
		if (proc.data == SECTION) {
			// A section continues to the section following it, if there is one.
			final CFlowNode nextUnit = proc.next(SECTION);

			// Did we get one ?
			if (!nextUnit.isNull())
				return nextUnit.asList();

			// ... If not, we could try the section's container; which would be
			// the procedure division. But we won't, as it won't change the
			// outcome in any way.
		}

		// At this point we're left with just declarative sections and the
		// procedure divisions itself. Exit nodes for either of these will never
		// have successors. Which is why we return an empty answer.
		return Collections.emptyList();
	}

	public String getName(Tree proc) {
		if (proc == null)
			return null;
		else
			return ((Start) proc.getData()).getName();
	}
}
