package com.semmle.cobol.cflow.logic;

import java.util.Collections;
import java.util.List;

import com.semmle.cobol.cflow.Wiring;
import com.semmle.cobol.generator.effects.CFlowNode;

public class GobackStatementLogic extends EmbeddedStatementLogic {

	public GobackStatementLogic(Wiring wiring) {
		super(wiring);
	}

	/**
	 * "1) If a <code>GOBACK</code> statement is executed in a program that is
	 * under the control of a calling runtime element, the program operates as
	 * if executing an <code>EXIT PROGRAM</code> statement with the
	 * <code>RAISING</code> phrase, if any, that is specified in the
	 * <code>GOBACK</code> statement." So that means exiting the current program
	 * and returning to its caller.
	 * <p>
	 * "2) If a <code>GOBACK</code> statement is executed in a program that is
	 * not under the control of a calling runtime element, the program operates
	 * as if executing a <code>STOP</code> statement without any optional
	 * phrases. A <code>RAISING</code> phrase, if specified, is ignored." That
	 * means exiting the current program and returning to the OS.
	 * <p>
	 * I take all that to mean there is no successor within the same program.
	 */
	@Override
	public List<CFlowNode> getSuccessors(CFlowNode n) {
		return Collections.emptyList();
	}

	/**
	 * A <code>GOBACK</code> statement has no continuations for the same reason
	 * that it has no successors.
	 */
	@Override
	public List<CFlowNode> getContinuations(CFlowNode n) {
		return Collections.emptyList();
	}
}
