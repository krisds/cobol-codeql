package com.semmle.cobol.generator.triggers;

import com.semmle.cobol.generator.events.Event;

/**
 * A {@link Trigger} which always fires.
 */
class OnAny extends BasicTrigger {

	@Override
	public TriggerState evaluate(Event event) {
		return TriggerState.FIRED;
	}

	@Override
	public String toString() {
		return "any";
	}
}