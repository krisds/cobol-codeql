package com.semmle.cobol.generator.triggers;

import static com.semmle.cobol.generator.events.Event.Type.TOKEN;

import com.semmle.cobol.generator.events.Event;

import koopa.core.data.Token;

/**
 * A {@link Trigger} which fires when the event is for a {@link Token}.
 */
class OnToken extends BasicTrigger {
	@Override
	public TriggerState evaluate(Event e) {
		return TriggerState.fromBoolean(e.type == TOKEN);
	}

	@Override
	public String toString() {
		return "token";
	}
}