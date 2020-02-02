package com.semmle.cobol.generator.effects;

import com.semmle.cobol.generator.engine.Frame;
import com.semmle.cobol.generator.engine.RuleEngine;
import com.semmle.cobol.generator.events.Event;

/**
 * This {@link Effect} pushes the values from the {@link Frame} it's being run
 * in to that frame's parent.
 */
class ReturnEffect implements Effect {
	@Override
	public void apply(Event event, Frame frame, RuleEngine engine) {
		frame.pushValuesToParent();
	}

	@Override
	public String toString() {
		return "return";
	}
}