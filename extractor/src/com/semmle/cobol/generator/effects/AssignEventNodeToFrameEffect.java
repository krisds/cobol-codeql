package com.semmle.cobol.generator.effects;

import com.semmle.cobol.generator.engine.Frame;
import com.semmle.cobol.generator.engine.RuleEngine;
import com.semmle.cobol.generator.events.Event;

/**
 * An {@link Effect} which assigns the {@link Event#getNode()} to the
 * {@link Frame#node}.
 */
class AssignEventNodeToFrameEffect implements Effect {
	@Override
	public void apply(Event event, Frame frame, RuleEngine engine) {
		frame.node = event.getNode();
	}

	@Override
	public String toString() {
		return "frame.node = event.node";
	}
}