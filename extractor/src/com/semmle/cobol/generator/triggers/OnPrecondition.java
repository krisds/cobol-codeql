package com.semmle.cobol.generator.triggers;

import static com.semmle.cobol.generator.events.Event.Type.START;

import com.semmle.cobol.generator.events.Event;
import com.semmle.cobol.generator.preconditions.Precondition;
import com.semmle.cobol.generator.preconditions.Precondition.Result;

import koopa.core.data.markers.Start;

/**
 * A {@link Trigger} which fires when the event is for a given
 * {@link Precondition}.
 */
class OnPrecondition extends BasicTrigger {

	private final Start subject;
	private final Precondition precondition;

	private boolean active = false;

	public OnPrecondition(Start subject, Precondition precondition) {
		this.subject = subject;
		this.precondition = precondition;
	}

	@Override
	public TriggerState evaluate(Event event) {
		if (active)
			if (subject == null && event.type == START || event.data == subject)
				return TriggerState.FIRED;

		if (event.data instanceof Result) {
			// There may be results for other preconditions. We
			// ignore those, and only become active when we see
			// a result for out precondition.
			if (((Result) event.data).precondition == precondition)
				active = true;
		} else
			active = false;

		return TriggerState.INACTIVE;
	}

	@Override
	public String toString() {
		return precondition.toString();
	}
}
