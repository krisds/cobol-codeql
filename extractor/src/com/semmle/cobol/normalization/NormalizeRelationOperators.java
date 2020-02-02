package com.semmle.cobol.normalization;

import static com.semmle.cobol.util.Common.EQUALS_OP;
import static com.semmle.cobol.util.Common.EQUAL_TO_OP;
import static com.semmle.cobol.util.Common.EXCEEDS_OP;
import static com.semmle.cobol.util.Common.GREATER_OR_EQUAL_OP;
import static com.semmle.cobol.util.Common.GREATER_THAN_OP;
import static com.semmle.cobol.util.Common.LESS_OR_EQUAL_OP;
import static com.semmle.cobol.util.Common.LESS_THAN_OP;
import static com.semmle.cobol.util.Common.NOT;
import static com.semmle.cobol.util.Common.RELOP;
import static com.semmle.cobol.util.Common.UNEQUALTO_OP;
import static com.semmle.cobol.util.Common._RELOP;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.semmle.cobol.extractor.StreamProcessingStep;

import koopa.core.data.Data;
import koopa.core.data.Token;
import koopa.core.data.markers.Start;

/**
 * The grammar for relation operators leaves <code>NOT</code> tokens outside of
 * the actual operator, such as <code>GREATER THAN</code>. So we get a structure
 * like:
 * <ul>
 * <li>relop
 * <ul>
 * <li>not</li>
 * <li>greaterThanOp</li>
 * </ul>
 * </li>
 * </ul>
 * This makes sense for the grammar, but complicates trapping in a streamed
 * style. The simplest solution is to catch these negated relation operators and
 * rewrite them. So the above should become:
 * <ul>
 * <li>relop
 * <ul>
 * <li>lessThanOrEqualToOp</li>
 * </ul>
 * </li>
 * </ul>
 * Which is straightforward to handle in a stream.
 */
public class NormalizeRelationOperators extends StreamProcessingStep {

	private static enum State {
		IDLE, IN_RELOP
	}

	/**
	 * This defines all relation operators as keys, and their negated
	 * counterparts as values. If there is no counterpart, because it can't be
	 * negated in the syntax, the value will be <code>null</code>.
	 */
	private static final Map<Start, Start> RELOPS_AND_THEIR_NEGATIONS = new LinkedHashMap<>();
	static {
		RELOPS_AND_THEIR_NEGATIONS.put(GREATER_OR_EQUAL_OP, LESS_THAN_OP);
		RELOPS_AND_THEIR_NEGATIONS.put(LESS_OR_EQUAL_OP, GREATER_THAN_OP);
		RELOPS_AND_THEIR_NEGATIONS.put(GREATER_THAN_OP, LESS_OR_EQUAL_OP);
		RELOPS_AND_THEIR_NEGATIONS.put(LESS_THAN_OP, GREATER_OR_EQUAL_OP);
		RELOPS_AND_THEIR_NEGATIONS.put(EQUAL_TO_OP, UNEQUALTO_OP);
		RELOPS_AND_THEIR_NEGATIONS.put(EXCEEDS_OP, null);
		RELOPS_AND_THEIR_NEGATIONS.put(EQUALS_OP, null);
		RELOPS_AND_THEIR_NEGATIONS.put(UNEQUALTO_OP, null);
	}

	private State state = State.IDLE;

	private List<Data> tokens = new LinkedList<Data>();
	private Start relop = null;
	private boolean negated = false;

	@Override
	public void push(Data d) {
		switch (state) {
		case IDLE:
			// We keep an eye out for relation operators.
			if (d == RELOP)
				state = State.IN_RELOP;
			else
				pass(d);

			break;

		case IN_RELOP:
			// While in a relation operator, we keep a hold of all tokens.
			if (d instanceof Token)
				tokens.add(d);

			// We need to track whether we saw negation or not.
			if (d == NOT)
				negated = true;

			else if (d == _RELOP) {
				// If the relation operator is complete, and it was negated, we
				// replace it with its negated counterpart.
				if (negated)
					relop = RELOPS_AND_THEIR_NEGATIONS.get(relop);

				// Now we must rebuild the stream by wrapping the tokens in the
				// right relation operator.
				pass(RELOP);
				pass(relop);
				for (Data r : tokens) {
					pass(r);
				}
				pass(relop.matchingEnd());
				pass(_RELOP);

				// After which we clear our own state and start scanning again
				// for more operators.
				state = State.IDLE;
				tokens.clear();
				relop = null;
				negated = false;

			} else if (RELOPS_AND_THEIR_NEGATIONS.containsKey(d))
				// We need to track the specific relation operator.
				relop = (Start) d;

			break;
		}
	}
}
