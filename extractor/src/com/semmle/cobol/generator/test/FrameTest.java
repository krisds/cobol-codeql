package com.semmle.cobol.generator.test;

import static com.semmle.cobol.generator.effects.Effects.RETURN;
import static com.semmle.cobol.generator.effects.Effects.all;
import static com.semmle.cobol.generator.effects.Effects.atEnd;
import static com.semmle.cobol.generator.effects.Effects.closure;
import static com.semmle.cobol.generator.effects.Effects.createTuple;
import static com.semmle.cobol.generator.effects.Effects.on;
import static com.semmle.cobol.generator.test.StreamUtil.token;
import static com.semmle.cobol.generator.test.StreamUtil.tree;
import static com.semmle.cobol.generator.triggers.Triggers.path;

import org.junit.Assert;
import org.junit.Test;

import com.semmle.cobol.generator.effects.Effect;
import com.semmle.cobol.generator.engine.Frame;
import com.semmle.cobol.generator.engine.RuleEngine;
import com.semmle.cobol.generator.events.Event;
import com.semmle.cobol.generator.rules.RuleSet;
import com.semmle.cobol.mapping.runtime.TrapFile;

/**
 * Unit tests which check the management of {@link Frame}s while running the
 * rule engine.
 */
public class FrameTest {

	@Test
	public void defaultFrameAndReturns() {
		final RuleEngine engine = new RuleEngine(new TrapFile(null));
		final RuleSet rules = new RuleSet();

		// An <add> node should return an @add tuple.
		// <add> = @add
		rules.define("<add>", all( //
				// Rule activation starts in a new frame.
				assertFrameIsEmpty(),

				// Here we create the new tuple.
				createTuple("add"),

				// The tuple and the associated node should have been saved in
				// the frame.
				assertFrameHasTuple("add"), //
				assertFrameHasNode("add") //
		));

		// A <stmt> node should return whatever tuple its first child node
		// returns.
		// <stmt> = on <>[1] : apply matching rule and return
		rules.define("<stmt>", all( //
				// Rule activation starts in a new frame.
				assertFrameIsEmpty(),

				on(path("<>[1]"), all( //
						// Trigger activation starts in an empty frame as well.
						assertFrameIsEmpty(), //
						// Here we ask to run the rule matching the current
						// node.
						rules.applyMatchingRule(), //
						atEnd(all(
								// A rule returns the tuple it as created.
								assertFrameHasTuple("add"), //
								assertFrameHasNode("add"), //
								// But the "on" won't push that value as the
								// return value for the "<stmt>" rule. We have
								// to do that ourselves.
								RETURN //
						)) //
				)),

				atEnd(all(
						// The RTN in the "on" should have made sure that the
						// rule's frame now also has the tuple generated for the
						// first child node.
						assertFrameHasTuple("add"), //
						assertFrameHasNode("add") //
				)) //
		));

		// The root node just tells the engine to process its child nodes.
		// <x> = on <> apply matching rule
		rules.define("<x>", all( //
				// Rule activation starts in a new frame.
				assertFrameIsEmpty(),

				on(path("<>"), all( //
						// Trigger activation starts in an empty frame as well.
						assertFrameIsEmpty(), //
						// We ask the engine to run the right rule.
						rules.applyMatchingRule(), //
						atEnd(all( //
									// A rule returns the tuple it as created.
								assertFrameHasTuple("add"), //
								assertFrameHasNode("add") //
						// Note that this time we don't ask to pass on the
						// generated tuple to the rule's frame.
						)) //
				)),

				atEnd( //
						// Because we didn't generate a tuple for this frame,
						// nor passed on a tuple from an "on", the frame should
						// still have no tuple assigned.
						assertFrameIsEmpty() //
				) //
		));

		engine.add(path("<x>"), closure(rules.applyMatchingRule()));

		tree("x", //
				tree("stmt", //
						tree("add", token("A"))) //
		).streamInto(engine);
		engine.done();
	}

	private Effect assertFrameIsEmpty() {
		return new Effect() {
			@Override
			public void apply(Event event, Frame frame, RuleEngine engine) {
				Assert.assertNotNull(event + ": expected a frame", frame);
				Assert.assertNull(event + ": expected no tuple", frame.tuple);
				Assert.assertNull(event + ": expected no node", frame.node);
			}

			@Override
			public String toString() {
				return "assert frame is empty";
			}
		};
	}

	private Effect assertFrameHasTuple(String name) {
		return new Effect() {
			@Override
			public void apply(Event event, Frame frame, RuleEngine engine) {
				Assert.assertNotNull(event + ": expected a frame", frame);
				Assert.assertNotNull(event + ": should have a tuple",
						frame.tuple);
				Assert.assertEquals(event + ": should have a tuple @" + name,
						name, frame.tuple.getName());
			}

			@Override
			public String toString() {
				return "assert frame has tuple @" + name;
			}
		};
	}

	private Effect assertFrameHasNode(String name) {
		return new Effect() {
			@Override
			public void apply(Event event, Frame frame, RuleEngine engine) {
				Assert.assertNotNull(event + ": expected a frame", frame);
				Assert.assertNotNull(event + ": should have a node",
						frame.node);
				Assert.assertEquals(
						event + ": should have a node <" + name + ">", name,
						frame.node.data.getName());
			}

			@Override
			public String toString() {
				return "assert frame has node " + name;
			}
		};
	}
}
