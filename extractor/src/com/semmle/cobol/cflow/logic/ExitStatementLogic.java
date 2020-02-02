package com.semmle.cobol.cflow.logic;

import static com.semmle.cobol.util.Common.CFLOW__EXIT_NODE;
import static com.semmle.cobol.util.Common.PARAGRAPH;
import static com.semmle.cobol.util.Common.PERFORM;
import static com.semmle.cobol.util.Common.SECTION;

import java.util.Collections;
import java.util.List;

import com.semmle.cobol.cflow.Wiring;
import com.semmle.cobol.generator.effects.CFlowNode;

public class ExitStatementLogic extends EmbeddedStatementLogic {

	public ExitStatementLogic(Wiring wiring) {
		super(wiring);
	}

	/**
	 * <h1>EXIT PROGRAM</h1>
	 * 
	 * An <code>EXIT PROGRAM</code> can either:
	 * <ul>
	 * <li>end execution of the subprogram, but only if the program is being run
	 * as a subprogram (via a <code>CALL</code> from another program). In that
	 * case there is no successor in the same program</li>
	 * <li>act as a <code>CONTINUE</code> statement, in case the program is not
	 * being run as a subprogram. In that case we should return the
	 * continuation.</li>
	 * </ul>
	 * Combining both leaves us only with the continuation option.
	 * 
	 * <h1>EXIT PARAGRAPH</h1>
	 * 
	 * "The execution of an <code>EXIT PARAGRAPH</code> statement causes control
	 * to be passed to an implicit <code>CONTINUE</code> statement immediately
	 * following the last explicit statement of the current paragraph, preceding
	 * any return mechanisms for that paragraph." Which translates to saying
	 * that we move to the continuation of the enclosing paragraph.
	 * 
	 * <h1>EXIT SECTION</h1>
	 * 
	 * "The execution of an <code>EXIT SECTION</code> statement causes control
	 * to be passed to an unnamed empty paragraph immediately following the last
	 * paragraph of the current section, preceding any return mechanisms for
	 * that section." Which translates to saying that we move to the
	 * continuation of the enclosing section.
	 * 
	 * <h1>EXIT PERFORM</h1>
	 * 
	 * "The execution of an <code>EXIT PERFORM</code> statement without the
	 * CYCLE phrase causes control to be passed to an implicit
	 * <code>CONTINUE</code> statement immediately following the
	 * <code>END-PERFORM</code> phrase that matches the most closely preceding,
	 * and as yet unterminated, inline <code>PERFORM</code> statement." Which
	 * translates to saying that we move to the continuation of the enclosing
	 * section.
	 * 
	 * <h1>EXIT PERFORM CYCLE</h1>
	 * 
	 * "The execution of an <code>EXIT PERFORM</code> statement with the
	 * <code>CYCLE</code> phrase causes control to be passed to an implicit
	 * <code>CONTINUE</code> statement immediately preceding the
	 * <code>END-PERFORM</code> phrase that matches the most closely preceding,
	 * and as yet unterminated, inline <code>PERFORM</code> statement." Which
	 * translates to another round of the enclosing <code>PERFORM</code>
	 * statement.
	 * 
	 * <h1>EXIT</h1>
	 * 
	 * "An <code>EXIT</code> statement serves only to enable the user to assign
	 * a procedure- name to a given point in a procedure division. Such an
	 * <code>EXIT</code> statement has no other effect on the compilation or
	 * execution." Which means the flow just continues past it.
	 */
	@Override
	public List<CFlowNode> getSuccessors(CFlowNode n) {
		final String name = n.getTuple().getStringAttribute("endpoint");

		if (name == null || "PROGRAM".equalsIgnoreCase(name)) {
			return getContinuations(n);

		} else if ("PARAGRAPH".equalsIgnoreCase(name)) {
			// Rather than going to an implicit CONTINUE at the end of the
			// paragraph, we'll go to the exit node we've established as the end
			// of the paragraph instead.
			return n.closest(PARAGRAPH).findLast(CFLOW__EXIT_NODE).asList();

		} else if ("SECTION".equalsIgnoreCase(name)) {
			// Rather than going to an implicit CONTINUE at the end of the
			// section, we'll go to the exit node we've established as the end
			// of the section instead.
			return n.closest(SECTION).findLast(CFLOW__EXIT_NODE).asList();

		} else if ("PERFORM".equalsIgnoreCase(name)) {
			final CFlowNode perform = n.closest(PERFORM);
			return super.getContinuations(perform);

		} else if ("PERFORM CYCLE".equalsIgnoreCase(name)) {
			final CFlowNode perform = n.closest(PERFORM);
			return Collections.singletonList(perform.getParent());

		} else {
			return Collections.emptyList();
		}
	}
}
