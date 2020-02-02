package com.semmle.cobol.generator.triggers;

import static com.semmle.cobol.generator.events.Event.Type.END;

import com.semmle.cobol.generator.events.Event;

import koopa.core.data.markers.End;

/**
 * A {@link Trigger} which fires when the event is for a {@link End}.
 */
class OnEnd extends BasicTrigger {
	@Override
	public TriggerState evaluate(Event e) {
		return TriggerState.fromBoolean(e.type == END);
	}

	@Override
	public String toString() {
		return "</>";
	}
}