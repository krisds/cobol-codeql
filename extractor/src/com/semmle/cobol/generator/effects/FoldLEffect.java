package com.semmle.cobol.generator.effects;

import static com.semmle.cobol.extractor.CobolExtractor.getAttribute;
import static com.semmle.cobol.extractor.CobolExtractor.getType;
import static com.semmle.cobol.generator.effects.Effects.all;
import static com.semmle.cobol.generator.effects.Effects.atEnd;
import static com.semmle.cobol.generator.effects.Effects.on;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.semmle.cobol.generator.Trap;
import com.semmle.cobol.generator.engine.Frame;
import com.semmle.cobol.generator.engine.RuleEngine;
import com.semmle.cobol.generator.events.Event;
import com.semmle.cobol.generator.events.TreePath.Node;
import com.semmle.cobol.generator.functions.Fn1;
import com.semmle.cobol.generator.triggers.TriggerDefinition;
import com.semmle.cobol.generator.tuples.Tuple;
import com.semmle.cobol.generator.types.Attribute;
import com.semmle.cobol.generator.types.Type;

import koopa.core.data.Data;
import koopa.core.data.Position;

/**
 * {@link Effect} which creates a binary tree of tuples for matching nodes.
 */
class FoldLEffect implements Effect {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(FoldLEffect.class);

	/**
	 * When should this effect be triggered ?
	 */
	private final TriggerDefinition def;

	/**
	 * How do we create a {@link Tuple} for matching nodes ?
	 */
	private final Effect createTuple;

	/**
	 * Given a matching node, what's the type name for the binary {@link Tuple}
	 * to be created ?
	 */
	private final Fn1<Data, String> nodeToBinaryTypeName;

	FoldLEffect(TriggerDefinition def, Effect createTuple,
			Fn1<Data, String> nodeToBinaryTypeName) {
		this.def = def;
		this.createTuple = createTuple;
		this.nodeToBinaryTypeName = nodeToBinaryTypeName;
	}

	@Override
	public void apply(Event event, Frame frame, RuleEngine engine) {
		// When we see a matching node we create the Tuple. Then at the end of
		// the matching subtree we fold that tuple into the binary tree.
		on(def, all( //
				createTuple, //
				atEnd(fold(event.getNode(), frame)) //
		)).apply(event, frame, engine);
	}

	private Effect fold(Node rootNode, Frame rootFrame) {
		return new Effect() {
			private Position start = null;

			@Override
			public void apply(Event event, Frame frame, RuleEngine engine) {
				final Tuple leftOperandTuple = rootFrame.tuple;
				final Tuple rightOperandTuple = frame.tuple;

				if (leftOperandTuple == null) {
					if (LOGGER.isTraceEnabled())
						LOGGER.trace("First value. No need to fold.");

					rootFrame.tuple = rightOperandTuple;
					rootFrame.node = rootNode;
					start = frame.node.start;

					return;
				}

				final Node node = event.getNode();
				final String typeName = nodeToBinaryTypeName.eval(node.data);
				final Type binary = getType(typeName);
				final Attribute left_operand = getAttribute(binary,
						"left_operand");
				final Attribute right_operand = getAttribute(binary,
						"right_operand");

				if (LOGGER.isTraceEnabled())
					LOGGER.trace("Folding " + leftOperandTuple + " with "
							+ rightOperandTuple);

				final Tuple binaryTuple = Trap.trapTuple(typeName, node, null,
						engine);

				if (LOGGER.isTraceEnabled())
					LOGGER.trace("Folding into " + binaryTuple);

				Trap.parentTupleToAttribute(binaryTuple, left_operand,
						leftOperandTuple);
				Trap.parentTupleToAttribute(binaryTuple, right_operand,
						rightOperandTuple);

				Trap.trapLocation(binaryTuple, start, node.end, engine);

				rootFrame.tuple = binaryTuple;
			}

			@Override
			public String toString() {
				return "foldl";
			}
		};
	}

	@Override
	public String toString() {
		return "foldl " + def;
	}
}