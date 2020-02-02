package com.semmle.cobol.generator.effects;

import com.semmle.cobol.generator.Trap;
import com.semmle.cobol.generator.engine.Frame;
import com.semmle.cobol.generator.engine.RuleEngine;
import com.semmle.cobol.generator.events.Event;
import com.semmle.cobol.generator.events.TreePath.Node;
import com.semmle.cobol.generator.tuples.Tuple;

/**
 * {@link Effect} which traps the location of the tuple on the active frame.
 */
class LocationEffect implements Effect {
	@Override
	public void apply(Event event, Frame frame, RuleEngine engine) {
		final Node node = frame.node;
		final Tuple tuple = frame.tuple;
		Trap.trapLocation(tuple, node, engine);
	}

	@Override
	public String toString() {
		return "location";
	}
}