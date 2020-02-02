package com.semmle.cobol.generator.effects;

import static com.semmle.cobol.extractor.CobolExtractor.getDefaultTypeName;
import static com.semmle.cobol.util.Common.COMPILER_STATEMENT;
import static com.semmle.cobol.util.Common.PERFORM;
import static com.semmle.cobol.util.Common.STATEMENT;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.semmle.cobol.cflow.CobolWiring;
import com.semmle.cobol.cflow.Logic;
import com.semmle.cobol.cflow.Wiring.Tag;
import com.semmle.cobol.generator.Trap;
import com.semmle.cobol.generator.engine.Frame;
import com.semmle.cobol.generator.engine.RuleEngine;
import com.semmle.cobol.generator.events.Event;
import com.semmle.cobol.generator.events.TreePath.Node;
import com.semmle.cobol.generator.triggers.BasicTrigger;
import com.semmle.cobol.generator.triggers.Trigger;
import com.semmle.cobol.generator.triggers.TriggerState;
import com.semmle.cobol.generator.tuples.Tuple;
import com.semmle.cobol.mapping.values.ReferenceValue;
import com.semmle.cobol.util.Common;
import com.semmle.util.exception.CatastrophicError;

import koopa.core.data.Data;

/**
 * A graph of {@link CFlowNode}s, with {@link Trigger}s and {@link Effect}s to
 * build the graph while the {@link RuleEngine} is running.
 */
class CFlowGraph {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(CFlowGraph.class);

	private static final boolean TRACE_ENABLED = LOGGER.isTraceEnabled();

	private static final CobolWiring WIRING = CobolWiring.INSTANCE;

	private static final Set<Data> CFLOW_NODES = new LinkedHashSet<>();
	static {
		CFLOW_NODES.addAll(WIRING.getNodeTypesTagged(Tag.CFLOW));
		// The WIRING lists only exception branches. So we add the missing ones.
		CFLOW_NODES.add(Common.WHEN);
		CFLOW_NODES.add(Common.WHEN_OTHER);
		CFLOW_NODES.add(Common.THEN);
		CFLOW_NODES.add(Common.ELSE);
		CFLOW_NODES.add(Common.NESTED_STATEMENTS);
	}

	/**
	 * A {@link Trigger} which fires on {@link Event}s which have a node listed
	 * in {@link #CFLOW_NODES}.
	 */
	public static final Trigger CFLOW_NODE = new BasicTrigger() {
		@Override
		public TriggerState evaluate(Event event) {
			return TriggerState.fromBoolean( //
					CFLOW_NODES.contains(event.data));
		}

		@Override
		public String toString() {
			return "cflow node";
		}
	};

	/**
	 * While building the graph, this stack holds the active subtrees under
	 * construction.
	 */
	private Stack<CFlowNode> stack = new Stack<>();

	/**
	 * This {@link Effect} starts the graph, and sets up the root node.
	 */
	public Effect start() {
		return new Effect() {
			@Override
			public void apply(Event event, Frame frame, RuleEngine engine) {
				final CFlowNode n = new CFlowNode(event.data, event.getNode());

				if (TRACE_ENABLED)
					LOGGER.trace("root {}", n);

				stack.push(n);
			}

			@Override
			public String toString() {
				return "start cflow graph";
			}
		};
	}

	/**
	 * This {@link Effect} adds a new subtree to the graph.
	 */
	public Effect pushNode() {
		return new Effect() {
			@Override
			public void apply(Event event, Frame frame, RuleEngine engine) {
				final CFlowNode n = new CFlowNode(event.data, event.getNode());

				if (TRACE_ENABLED)
					LOGGER.trace("pushing {}", n);

				stack.peek().add(n);
				stack.push(n);
			}

			@Override
			public String toString() {
				return "push cflow node";
			}
		};
	}

	/**
	 * This {@link Effect} completes the current subtree.
	 */
	public Effect popNode() {
		return new Effect() {
			@Override
			public void apply(Event event, Frame frame, RuleEngine engine) {
				final CFlowNode n = popWithTuple(event, engine);

				if (TRACE_ENABLED)
					LOGGER.trace("popped {} ~ {}", n, n.tuple);
			}

			@Override
			public String toString() {
				return "push cflow node";
			}
		};
	}

	/**
	 * This effect finalizes the graph, and then traps all successor relations
	 * found in the graph.
	 */
	public Effect finish() {
		return new Effect() {
			@Override
			public void apply(Event event, Frame frame, RuleEngine engine) {
				final CFlowNode root = popWithTuple(event, engine);

				if (TRACE_ENABLED)
					LOGGER.trace("finished with {} ~ {}", root, root.tuple);

				for (CFlowNode n : root.depthFirst()) {
					if (!WIRING.participatesInCFlow(n.data)) {
						if (TRACE_ENABLED)
							LOGGER.trace("- {} ~ {}", n, n.tuple);
						continue;
					}

					if (TRACE_ENABLED)
						LOGGER.trace("+ {} ~ {}", n, n.tuple);

					final Logic logic = WIRING.getLogic(n.data);
					final List<CFlowNode> successors = logic.getSuccessors(n);
					for (CFlowNode succ : successors) {
						if (TRACE_ENABLED)
							LOGGER.trace("=> {}", succ);

						final Tuple tuple = Trap.trapTuple("successor", n.node,
								"successor-" + succ.node.hashCode(), engine);

						if (LOGGER.isDebugEnabled())
							LOGGER.debug("{} == {} --> {}", tuple, n.tuple,
									succ.tuple);

						if (succ.tuple == null)
							throw new CatastrophicError(n + " --> " + succ);

						tuple.addValue(
								new ReferenceValue("predecessor", n.tuple));
						tuple.addValue(
								new ReferenceValue("successor", succ.tuple));
					}
				}
			}

			@Override
			public String toString() {
				return "finish cflow graph";
			}
		};
	}

	/**
	 * Pop the current subtree from the stack, and set the tuple for it.
	 */
	private CFlowNode popWithTuple(Event event, RuleEngine engine) {
		final CFlowNode n = stack.pop();

		// Special case: (compiler) statements should have the tuple (and node)
		// of their embedded statement.
		if (n.data == STATEMENT || n.data == COMPILER_STATEMENT) {
			final CFlowNode embedded = n.firstChild();
			n.tuple = embedded.tuple;
			n.node = embedded.node;
			return n;
		}

		final Node node = event.getNode();

		// Special case: PERFORM statements can have two tuple types. We just
		// try both as needed.
		if (n.data == PERFORM) {
			n.tuple = Trap.getExistingTuple("perform_inline", node, null,
					engine);

			if (n.tuple == null)
				n.tuple = Trap.getExistingTuple("perform_outofline", node, null,
						engine);

			return n;
		}

		// We ask what type of tuple we're looking for.
		String typeName = getDefaultTypeName(n.data);

		// Special case: unknown embedded statements have the @other_stmt type.
		if (typeName == null && node.parent.data == Common.STATEMENT) {
			typeName = "other_stmt";
		}

		n.tuple = Trap.getExistingTuple(typeName, node, null, engine);
		return n;
	}
}
