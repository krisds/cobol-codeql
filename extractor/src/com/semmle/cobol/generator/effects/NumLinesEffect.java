package com.semmle.cobol.generator.effects;

import static com.semmle.cobol.generator.effects.Effects.all;
import static com.semmle.cobol.generator.effects.Effects.atEnd;
import static com.semmle.cobol.generator.effects.Effects.on;
import static com.semmle.cobol.generator.triggers.Triggers.TOKEN;
import static koopa.cobol.data.tags.CobolAreaTag.IDENTIFICATION_AREA;
import static koopa.cobol.data.tags.CobolAreaTag.INDICATOR_AREA;
import static koopa.cobol.data.tags.CobolAreaTag.SEQUENCE_NUMBER_AREA;
import static koopa.cobol.data.tags.CobolTag.SOURCE_LISTING_DIRECTIVE;
import static koopa.core.data.tags.AreaTag.COMMENT;
import static koopa.core.data.tags.AreaTag.COMPILER_DIRECTIVE;
import static koopa.core.data.tags.AreaTag.PROGRAM_TEXT_AREA;
import static koopa.core.data.tags.AreaTag.SKIPPED;
import static koopa.core.data.tags.IslandTag.LAND;
import static koopa.core.data.tags.IslandTag.WATER;
import static koopa.core.data.tags.SyntacticTag.END_OF_LINE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.semmle.cobol.generator.Tally;
import com.semmle.cobol.generator.Trap;
import com.semmle.cobol.generator.engine.Frame;
import com.semmle.cobol.generator.engine.RuleEngine;
import com.semmle.cobol.generator.events.Event;
import com.semmle.cobol.generator.events.TreePath.Node;
import com.semmle.cobol.generator.tuples.Tuple;
import com.semmle.cobol.normalization.NormalizationTag;

import koopa.core.data.Token;

/**
 * An {@link Effect} which calculates the number of lines, lines of code, lines
 * of comments and lines of water in a certain scope, then traps a tuple with
 * the results.
 */
class NumLinesEffect implements Effect {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(NumLinesEffect.class);

	@Override
	public void apply(Event event, Frame frame, RuleEngine engine) {
		final Tally tally = new Tally();
		all( //
				on(TOKEN, tally(tally)), //
				atEnd(finalizeTally(tally)) //
		).apply(event, frame, engine);
	}

	private Effect tally(Tally tally) {
		return new Effect() {
			@Override
			public void apply(Event event, Frame frame, RuleEngine engine) {
				final Token token = (Token) event.data;

				if (token.hasTag(NormalizationTag.COMPILER_GENERATED))
					return;

				if (token.getStart().getLinenumber() > tally.lineNumber) {
					if (tally.sawCode)
						tally.code += 1;
					if (tally.sawComment)
						tally.comments += 1;
					if (tally.sawWater)
						tally.water += 1;

					tally.lines = tally.lines + 1;
					tally.sawCode = false;
					tally.sawComment = false;
					tally.sawWater = false;
				}

				if (token.hasAnyTag(SKIPPED, INDICATOR_AREA,
						SEQUENCE_NUMBER_AREA, IDENTIFICATION_AREA,
						END_OF_LINE)) {
					// Neither code nor comment

				} else if (token.hasTag(LAND)) {
					if (token.hasTag(COMMENT))
						tally.sawComment = true;

					else if (token.hasAnyTag(PROGRAM_TEXT_AREA,
							COMPILER_DIRECTIVE, SOURCE_LISTING_DIRECTIVE))
						tally.sawCode = true;

					else {
						if (LOGGER.isDebugEnabled())
							LOGGER.debug(
									"Code or comment ? " + token.toString());
					}

				} else if (token.hasTag(WATER)) {
					if (token.hasTag(COMMENT))
						tally.sawComment = true;

					else if (token.hasTag(PROGRAM_TEXT_AREA)) {
						tally.sawCode = true;
						tally.sawWater = true;

					} else {
						tally.sawWater = true;
						if (LOGGER.isDebugEnabled())
							LOGGER.debug(
									"Code or comment ? " + token.toString());
					}

				} else {
					if (LOGGER.isDebugEnabled())
						LOGGER.debug("Code or comment ? " + token.toString());
				}

				tally.lineNumber = token.getStart().getLinenumber();
			}

			@Override
			public String toString() {
				return "numlines tally token";
			}
		};
	}

	private Effect finalizeTally(Tally tally) {
		return new Effect() {
			@Override
			public void apply(Event event, Frame frame, RuleEngine engine) {
				if (tally.sawCode)
					tally.code += 1;
				if (tally.sawComment)
					tally.comments += 1;
				if (tally.sawWater)
					tally.water += 1;

				final Tuple tuple = frame.tuple;
				final Node node = frame.node;

				if (LOGGER.isTraceEnabled())
					LOGGER.trace("Counted {} for {}", tally, tuple);

				Trap.trapNumLines(tuple, node, tally, engine);
			}

			@Override
			public String toString() {
				return "numlines finalize tally";
			}
		};
	}

	@Override
	public String toString() {
		return "numlines";
	}
}
