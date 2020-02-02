package com.semmle.cobol.generator.effects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.semmle.cobol.generator.engine.Frame;
import com.semmle.cobol.generator.engine.RuleEngine;
import com.semmle.cobol.generator.events.Event;

/**
 * {@link Effect} which sets the {@link Frame#mayBeOmitted} flag.
 */
class MayBeOmittedEffect implements Effect {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(MayBeOmittedEffect.class);

	@Override
	public void apply(Event event, Frame frame, RuleEngine engine) {
		if (LOGGER.isTraceEnabled())
			LOGGER.trace("Omitting tuple for: " + event.getNode());

		frame.node = event.getNode();
		frame.mayBeOmitted = true;
	}

	@Override
	public String toString() {
		return "omitted";
	}
}