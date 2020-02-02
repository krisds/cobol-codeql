package com.semmle.cobol.generator.triggers;

import static com.semmle.cobol.generator.events.Event.Type.TOKEN;

import com.semmle.cobol.generator.events.Event;

import koopa.core.data.Token;
import koopa.core.data.tags.AreaTag;

/**
 * A {@link Trigger} which fires when the event is for a {@link Token} which has
 * the {@linkplain AreaTag#PROGRAM_TEXT_AREA} tag.
 */
class OnProgramText extends BasicTrigger {
	@Override
	public TriggerState evaluate(Event e) {
		if (e.type != TOKEN)
			return TriggerState.INACTIVE;

		final Token t = (Token) e.data;
		return TriggerState.fromBoolean(t.hasTag(AreaTag.PROGRAM_TEXT_AREA));
	}

	@Override
	public String toString() {
		return "program text";
	}
}