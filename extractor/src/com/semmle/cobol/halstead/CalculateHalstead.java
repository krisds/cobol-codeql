package com.semmle.cobol.halstead;

import static com.semmle.cobol.extractor.CobolExtractor.getDefaultTypeName;
import static com.semmle.cobol.generator.events.Event.Type.HALSTEAD;
import static com.semmle.cobol.util.Common.ALPHANUMERIC_LITERAL;
import static com.semmle.cobol.util.Common.COBOL_WORD;
import static com.semmle.cobol.util.Common.DATA_DIVISION;
import static com.semmle.cobol.util.Common.DECIMAL;
import static com.semmle.cobol.util.Common.INTEGER_LITERAL;
import static com.semmle.cobol.util.Common.PICTURE_STRING;
import static com.semmle.cobol.util.Common.PROCEDURE_DIVISION;
import static com.semmle.cobol.util.Common.PROGRAM_DEFINITION;
import static com.semmle.cobol.util.Common.SPACE;
import static com.semmle.cobol.util.Common.ZERO;
import static com.semmle.cobol.util.Common._PROGRAM_DEFINITION;
import static koopa.core.data.tags.AreaTag.PROGRAM_TEXT_AREA;
import static koopa.core.data.tags.SyntacticTag.SEPARATOR;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.semmle.cobol.extractor.StreamProcessingStep;
import com.semmle.cobol.generator.Trap;
import com.semmle.cobol.generator.effects.Effect;
import com.semmle.cobol.generator.engine.Frame;
import com.semmle.cobol.generator.engine.RuleEngine;
import com.semmle.cobol.generator.events.Event;
import com.semmle.cobol.generator.events.TreePath.Node;
import com.semmle.cobol.generator.triggers.BasicTrigger;
import com.semmle.cobol.generator.triggers.Trigger;
import com.semmle.cobol.generator.triggers.TriggerState;
import com.semmle.cobol.generator.tuples.Tuple;
import com.semmle.cobol.mapping.values.ReferenceValue;
import com.semmle.cobol.normalization.NormalizationTag;
import com.semmle.util.exception.CatastrophicError;

import koopa.core.data.Data;
import koopa.core.data.Token;
import koopa.core.data.markers.End;
import koopa.core.data.markers.Start;
import koopa.core.data.tags.AreaTag;

public class CalculateHalstead extends StreamProcessingStep {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(CalculateHalstead.class);

	private static enum State {
		IDLE, PROCESSING_TARGET, PROCESSING_OPERAND
	}

	private State state = State.IDLE;

	/**
	 * Active tracking data for nested scopes. We will do the tracking on the
	 * active scope only, and update any parent scope once we leave it.
	 */
	private Stack<Tokens> tokens = new Stack<>();

	/**
	 * When inside a target, how do we recognize its end ?
	 */
	private End endOfCurrentTarget = null;

	/**
	 * When inside an operand, how do we recognize its end ?
	 */
	private End endOfCurrentOperand = null;

	/**
	 * What is the operand's name ?
	 */
	private StringBuilder operandText = new StringBuilder();

	/**
	 * While scanning the syntax tree: which subtrees are valid targets for
	 * being counted ?
	 * <p>
	 * Note that these subtrees won't be scanned for further targets.
	 */
	public static final Set<Start> TARGET_UNITS = new LinkedHashSet<>();
	static {
		TARGET_UNITS.add(DATA_DIVISION);
		TARGET_UNITS.add(PROCEDURE_DIVISION);
	}

	/**
	 * When counting tokens, which types count as operands ?
	 */
	public static final Set<Start> OPERAND_TYPES = new LinkedHashSet<Start>();
	static {
		OPERAND_TYPES.add(COBOL_WORD);
		OPERAND_TYPES.add(PICTURE_STRING);
		OPERAND_TYPES.add(INTEGER_LITERAL);
		OPERAND_TYPES.add(ALPHANUMERIC_LITERAL);
		OPERAND_TYPES.add(DECIMAL);
		OPERAND_TYPES.add(ZERO);
		OPERAND_TYPES.add(SPACE);
	}

	@Override
	public void push(Data d) {
		switch (state) {
		case IDLE:
			// In the IDLE state we're just looking for start and end of program
			// definitions, which groups stats, or we look for the start of a
			// target to process.

			if (d == PROGRAM_DEFINITION) {
				// Start of a new scope to collect stats for.
				if (LOGGER.isTraceEnabled())
					LOGGER.trace("HALSTEAD: start scope: {}", d);

				tokens.push(new Tokens());

			} else if (d == _PROGRAM_DEFINITION) {
				// Close the current scope, possibly reactivating a parent one.
				if (LOGGER.isTraceEnabled())
					LOGGER.trace("HALSTEAD: end scope: {}", d);

				pass(tally(tokens.pop()));

			} else if (TARGET_UNITS.contains(d)) {
				// Start of a new scope to collect stats for.
				if (LOGGER.isTraceEnabled())
					LOGGER.trace("HALSTEAD: start target: {}", d);

				tokens.push(new Tokens());

				// We need to know how to recognize the end of this new scope.
				endOfCurrentTarget = ((Start) d).matchingEnd();
				state = State.PROCESSING_TARGET;
			}
			break;

		case PROCESSING_TARGET:
			// While processing a target we look for operands, we check tokens,
			// and we wait for the end of the target.

			if (d == endOfCurrentTarget) {
				// Close the current scope, possibly reactivating a parent one.
				if (LOGGER.isTraceEnabled())
					LOGGER.trace("HALSTEAD: end target: {}", d);

				pass(tally(tokens.pop()));

				endOfCurrentTarget = null;
				state = State.IDLE;

			} else if (OPERAND_TYPES.contains(d)) {
				// Start of an operand.
				if (LOGGER.isTraceEnabled())
					LOGGER.trace("HALSTEAD: start operand: {}", d);

				state = State.PROCESSING_OPERAND;
				// We need to know how to recognize the end of this operand.
				endOfCurrentOperand = ((Start) d).matchingEnd();

			} else if (d instanceof Token) {
				final Token t = (Token) d;

				// Non-whitespace, non compiler-generated tokens count toward
				// operators, except when it's FILLER.
				if (!t.hasTag(NormalizationTag.COMPILER_GENERATED)
						&& !isConsideredWhitespace(t)) {

					final String text = t.getText();

					// FILLER is a name given to a data item when the programmer
					// doesn't care about the actual name. But it is used as an
					// operand.
					if ("FILLER".equalsIgnoreCase(text)) {
						if (LOGGER.isTraceEnabled())
							LOGGER.trace("HALSTEAD: FILLER operand: {}", t);
						tokens.peek().addOperand(text);

					} else {
						if (LOGGER.isTraceEnabled())
							LOGGER.trace("HALSTEAD: operator: {}", t);
						tokens.peek().addOperator(text);
					}
				}
			}

			break;

		case PROCESSING_OPERAND:
			// While processing an operand, we collect the program text, until
			// we have reached the operand's end.

			if (d == endOfCurrentOperand) {
				if (LOGGER.isTraceEnabled())
					LOGGER.trace("HALSTEAD: end operand: {} {}", d,
							operandText.toString());

				tokens.peek().addOperand(operandText.toString());

				operandText.setLength(0);
				endOfCurrentOperand = null;
				state = State.PROCESSING_TARGET;

			} else if (d instanceof Token) {
				final Token t = (Token) d;
				if (t.hasTag(AreaTag.PROGRAM_TEXT_AREA))
					operandText.append(t.getText());
			}

			break;
		}

		pass(d);
	}

	private HalsteadCount tally(Tokens ts) {
		// If there is a parent scope, we need to add the counts for the nested
		// scope to it.
		if (!tokens.isEmpty())
			tokens.peek().include(ts);

		return new HalsteadCount(ts);
	}

	/**
	 * A {@linkplain Trigger} which activates when it sees Halstead data in the
	 * stream.
	 */
	public static final Trigger HALSTEAD_DATA = new BasicTrigger() {
		@Override
		public TriggerState evaluate(Event event) {
			return TriggerState.fromBoolean(event.type == HALSTEAD);
		}

		@Override
		public String toString() {
			return "halstead";
		}
	};

	/**
	 * An {@linkplain Effect} which takes the Halstead count and creates a tuple
	 * for it in the trap file.
	 */
	public static final Effect TRAP_HALSTEAD_DATA = new Effect() {
		@Override
		public void apply(Event event, Frame frame, RuleEngine engine) {
			final HalsteadCount counts = (HalsteadCount) event.data;
			final Node node = event.getNode();

			final Tuple tuple = Trap.trapTuple("halstead_counts", node, null,
					engine);

			final String typeName = getDefaultTypeName(node.data);
			if (typeName == null)
				throw new CatastrophicError(
						"Don't know what type to use for " + node.data);

			final Tuple referenced = engine.getTrapFile().getTuple(typeName,
					node);

			tuple.addValue(
					new ReferenceValue("halstead_countable", referenced, "id"));
			tuple.addConstantValue("n1", counts.n1);
			tuple.addConstantValue("n2", counts.n2);
			tuple.addConstantValue("N1", counts.N1);
			tuple.addConstantValue("N2", counts.N2);

			if (LOGGER.isDebugEnabled())
				LOGGER.debug("halstead counts for {} is {} ({})", referenced,
						tuple, counts);
		}

		@Override
		public String toString() {
			return "halstead";
		}
	};

	/**
	 * While we may be counting many tokens, we don't want to count things which
	 * are just considered to be whitespace.
	 */
	private static boolean isConsideredWhitespace(Token token) {
		if (!token.hasTag(PROGRAM_TEXT_AREA))
			return true;

		// Or comma and semicolon separators:
		if (token.hasTag(SEPARATOR)
				&& (";".equals(token.getText()) || ",".equals(token.getText())
						|| token.getText().trim().length() == 0))
			return true;

		return false;
	}
}
