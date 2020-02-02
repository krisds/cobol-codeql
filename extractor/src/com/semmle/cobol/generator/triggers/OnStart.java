package com.semmle.cobol.generator.triggers;

import static com.semmle.cobol.generator.events.Event.Type.START;

import com.semmle.cobol.generator.events.Event;

import koopa.core.data.markers.Start;

/**
 * A {@link Trigger} which fires when the event is for a {@link Start}.
 */
class OnStart extends BasicTrigger {
	@Override
	public TriggerState evaluate(Event e) {
		return TriggerState.fromBoolean(e.type == START);
	}

	@Override
	public String toString() {
		return "<>";
	}
}