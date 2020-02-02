package com.semmle.cobol.generator.engine;

import static com.semmle.cobol.util.Common.COMMENT_ENTRY;
import static com.semmle.cobol.util.Common._COMMENT_ENTRY;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.semmle.cobol.extractor.CobolExtractor;
import com.semmle.cobol.generator.Trap;
import com.semmle.cobol.generator.effects.Effect;
import com.semmle.cobol.generator.events.Event;
import com.semmle.cobol.generator.events.TreePath.Node;
import com.semmle.cobol.generator.tuples.Tuple;
import com.semmle.cobol.generator.types.Attribute;
import com.semmle.cobol.generator.types.Partition;
import com.semmle.cobol.generator.types.Type;
import com.semmle.cobol.generator.types.TypeWithAttributes;
import com.semmle.cobol.mapping.values.ReferenceValue;
import com.semmle.cobol.normalization.NormalizationTag;

import koopa.core.data.Data;
import koopa.core.data.Position;
import koopa.core.data.Token;
import koopa.core.data.Tokens;
import koopa.core.data.markers.End;
import koopa.core.data.tags.AreaTag;
import koopa.core.data.tags.SyntacticTag;

/**
 * An {@link Event} handler which looks for comments and traps them.
 */
class TrapComments {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(TrapComments.class);

	private static enum State {
		IDLE, SKIPPING, COLLECTING_COMMENT
	}

	private final RuleEngine engine;

	private State state = State.IDLE;

	// Need this to be able to detect inline comments, and handle their scope.
	private Token previousProgramText = null;
	private Node previousProgramNode = null;

	// Need this to handle regular comments, whose scope comes after them.
	private List<Token> pendingComments = new LinkedList<>();

	private End skipTo = null;

	private List<Token> collection = new LinkedList<>();

	public TrapComments(RuleEngine engine) {
		this.engine = engine;
	}

	public void process(Event event) {
		if (event.type != Event.Type.START && event.type != Event.Type.END
				&& event.type != Event.Type.TOKEN)
			return;

		final Data d = event.data;

		switch (state) {
		case IDLE:
			if (d == COMMENT_ENTRY) {
				// Ignoring these for now.
				skipTo = _COMMENT_ENTRY;
				state = State.SKIPPING;
				return;
			}

			if (event.type != Event.Type.TOKEN) {
				if (!pendingComments.isEmpty()) {
					final Node scope = event.type == Event.Type.END ? null
							: event.getNode();
					engine.atEnd(trapComments(pendingComments, false, scope));
					pendingComments = new LinkedList<>();
				}
				return;
			}

			final Token t = (Token) d;

			if (t.hasTag(NormalizationTag.COMPILER_GENERATED)) {
				// These were added by the extractor/normalization, and should
				// be ignored.
				return;
			}

			if (t.hasTag(AreaTag.COMMENT)) {
				collection.add(t);
				state = State.COLLECTING_COMMENT;
				return;
			}

			if (!t.hasTag(AreaTag.PROGRAM_TEXT_AREA)) {
				previousProgramText = null;
				previousProgramNode = null;
			} else if (!t.hasTag(SyntacticTag.WHITESPACE)) {
				previousProgramText = t;
				previousProgramNode = event.getNode();
			}

			return;

		case SKIPPING:
			if (d == skipTo) {
				skipTo = null;
				state = State.IDLE;
			}

			return;

		case COLLECTING_COMMENT:
			if (d instanceof Token && ((Token) d).hasTag(AreaTag.COMMENT)) {
				collection.add((Token) d);
				return;
			}

			final Token comment = Tokens.join(collection,
					collection.get(0).getTags());

			final String text = comment.getText();
			if (!text.trim().isEmpty()) {
				if (isInlineComment(previousProgramText, collection.get(0))) {
					if (LOGGER.isTraceEnabled())
						LOGGER.trace("INLINE : " + comment);

					// When the previous program node is complete we will trap
					// the inline comment.
					engine.atEndOf(previousProgramNode,
							trapComments(Collections.singletonList(comment),
									true, previousProgramNode));

				} else {
					if (LOGGER.isTraceEnabled())
						LOGGER.trace("PENDNG : " + comment);
					pendingComments.add(comment);
				}
			}

			collection.clear();

			// Still need to handle d.
			state = State.IDLE;

			process(event);
			return;
		}
	}

	private Effect trapComments(List<Token> comments, boolean inline,
			Node node) {
		return new Effect() {
			@Override
			public void apply(Event event, Frame frame, RuleEngine engine) {
				for (Token comment : comments) {
					final Tuple basicCommentTuple = trapComment(comment,
							inline);

					if (node != null)
						trapCommentScope(comment, node, basicCommentTuple);
				}
			}

			@Override
			public String toString() {
				return "trap comments";
			}
		};
	}

	private static boolean isInlineComment(Token previousProgramText,
			final Token comment) {
		return previousProgramText != null && previousProgramText.getEnd()
				.getLinenumber() == comment.getStart().getLinenumber();
	}

	private Tuple trapComment(Token comment, boolean inline) {
		if (LOGGER.isTraceEnabled())
			LOGGER.trace("Comment : " + comment);

		final Tuple basicCommentTuple = Trap.trapTuple("basic_comment", comment,
				null, engine);

		if (LOGGER.isTraceEnabled())
			LOGGER.trace("basic comment tuple = " + basicCommentTuple);

		final Position start = comment.getStart();
		final Position end = comment.getEnd();

		final Tuple locationsDefaultTupleForComment = Trap
				.trapLocation(basicCommentTuple, start, end, engine);

		if (LOGGER.isTraceEnabled())
			LOGGER.trace("locations default tuple = "
					+ locationsDefaultTupleForComment + " from " + start
					+ " to " + end);

		String commentText = comment.getText();
		if (inline && commentText.startsWith("*>"))
			commentText = commentText.substring(2);

		trapCommentText(comment, basicCommentTuple, commentText);

		return basicCommentTuple;
	}

	private void trapCommentText(Token comment, Tuple basicCommentTuple,
			String text) {

		final Attribute textAttribute = ((TypeWithAttributes) CobolExtractor
				.getType("comment")).getAttribute("text");

		final Tuple commentTextTuple = Trap.trapTuple(textAttribute, comment,
				engine);

		if (LOGGER.isTraceEnabled())
			LOGGER.trace("comment text tuple = " + commentTextTuple);

		Trap.parentTupleToAttribute(basicCommentTuple, textAttribute,
				commentTextTuple);

		final Type type = CobolExtractor.getType(textAttribute);
		if (type instanceof Partition) {
			// Because gentools.py may partition primitive values by
			// attribute...
			Partition p = (Partition) type;
			commentTextTuple.addConstantValue(p.getValueColumn(), text);

		} else
			commentTextTuple.addConstantValue("value", text);
	}

	private void trapCommentScope(Token comment, Node scope,
			final Tuple basicCommentTuple) {
		if (LOGGER.isTraceEnabled())
			LOGGER.trace("Comment scope : " + scope.data);

		final Tuple commentScope = Trap.trapTuple("commentScopes", comment,
				"commentScopes", engine);

		if (LOGGER.isTraceEnabled())
			LOGGER.trace("comment scope tuple = " + commentScope);

		final Tuple locationsDefaultTupleForScope = Trap.trapLocationsDefault(
				commentScope, scope.start, scope.end, engine);

		if (LOGGER.isTraceEnabled())
			LOGGER.trace("locations default tuple for comment scope = "
					+ locationsDefaultTupleForScope + " from " + scope.start
					+ " to " + scope.end);

		commentScope.addValue(new ReferenceValue("comment", basicCommentTuple));
		commentScope.addValue(
				new ReferenceValue("location", locationsDefaultTupleForScope));
	}
}
