package com.semmle.cobol.normalization;

import static com.semmle.cobol.util.Common._CFLOW__EXIT_NODE;
import static com.semmle.cobol.util.Common.CFLOW__EXIT_NODE;
import static com.semmle.cobol.util.Common._DECLARATIVE_SECTION;
import static com.semmle.cobol.util.Common._PARAGRAPH;
import static com.semmle.cobol.util.Common._PROCEDURE_DIVISION;
import static com.semmle.cobol.util.Common.PROCEDURE_DIVISION;
import static com.semmle.cobol.util.Common._SECTION;
import static com.semmle.cobol.util.Common.isDot;

import koopa.core.data.Data;
import koopa.core.data.Token;

/**
 * The last dot found in any section, paragraph, declarative section or
 * procedure division gets wrapped in a new "exit_node" tree node. This exit
 * node will become part of the successor relationship, marking the end of any
 * section, paragraph, declarative section and procedure division.
 */
public class AddControlFlowExitNodes extends AddImplicitSentence {

	private static enum State {
		WAITING_FOR_PROCEDURE_DIVISION, LOOKING_FOR_ENDINGS
	}

	private State state = State.WAITING_FOR_PROCEDURE_DIVISION;

	private Token closingDot;

	@Override
	public void push(Data d) {
		switch (state) {
		case LOOKING_FOR_ENDINGS:
			if (d == _PROCEDURE_DIVISION) {
				// Whenever we find the end of the procedure division, we
				// generate a cflow exit wherever we saw the last dot (if any).
				passPossibleCFlowExitAndAllDelayed();
				state = State.WAITING_FOR_PROCEDURE_DIVISION;

			} else if (d == _DECLARATIVE_SECTION || d == _SECTION
					|| d == _PARAGRAPH) {
				// Whenever we find the end of a (declaratives) section or
				// paragraph, we generate a cflow exit wherever we saw the last
				// dot (if any).
				passPossibleCFlowExitAndAllDelayed();
			}

			if (closingDot == null) {
				// While we're looking for a dot, pass any data until we found
				// it.
				if (isDot(d))
					closingDot = (Token) d;
				else
					pass(d);

			} else {
				// While we have found a dot, delay any data we see. If we see
				// another dot, pass the delayed data first.
				if (isDot(d)) {
					pass(closingDot);
					passAllDelayed();
					closingDot = (Token) d;
				} else
					delay(d);
			}
			break;

		case WAITING_FOR_PROCEDURE_DIVISION:
		default:
			if (d == PROCEDURE_DIVISION)
				state = State.LOOKING_FOR_ENDINGS;
			pass(d);
			break;
		}
	}

	private void passPossibleCFlowExitAndAllDelayed() {
		if (closingDot != null) {
			pass(CFLOW__EXIT_NODE);
			pass(closingDot);
			pass(_CFLOW__EXIT_NODE);
			passAllDelayed();

			// Reset to null, so we only do this once on any closing dot.
			closingDot = null;
		}
	}
}
